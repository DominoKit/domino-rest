/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.shared.request;

import static java.util.Objects.isNull;

import java.util.Map;
import java.util.logging.Logger;
import org.dominokit.rest.shared.regex.DominoCompiledPattern;
import org.dominokit.rest.shared.regex.RegexEngine;
import org.dominokit.rest.shared.request.exception.PathParameterMissingException;

/**
 * Formats a URL template by replacing expressions in:
 *
 * <ul>
 *   <li><b>Path segment names</b> (values from {@code pathParams})
 *   <li><b>Matrix parameters</b> names &amp; values (from {@code matrixParams})
 *   <li><b>Query</b> names &amp; values (from {@code queryParams})
 *   <li><b>Fragment</b> content (from {@code fragmentParams})
 * </ul>
 *
 * <p>Supported expression forms:
 *
 * <pre>
 *   :name
 *   {name}
 *   {name:regex}  // 'regex' must fully match the value
 * </pre>
 *
 * <p>Missing values throw {@link PathParameterMissingException}. Regex mismatches throw {@link
 * IllegalArgumentException}. After replacements, the URL is normalized via {@link ServicePath}.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>No automatic URL-encoding is performed; values are inserted verbatim.
 *   <li>Leading slashes are handled by {@link ServicePath#value()} — do not add one here.
 *   <li>The actual portion of the input URL that is modified is controlled by {@link
 *       RestConfig#getRegexEngine()} .
 * </ul>
 */
public class UrlFormatter {

  private static final Logger LOGGER = Logger.getLogger(UrlFormatter.class.getName());

  // Positional-capture pattern (portable across JVM & GWT):
  //   group(1) => :name
  //   group(2) => {name or name:regex}'s name
  //   group(3) => {name:regex}'s regex (optional)
  private static final String EXPR_PATTERN =
      "(?::([A-Za-z0-9_.-]+))" // :name -> group 1
          + "|\\{([A-Za-z0-9_.-]+)" // {name
          + "(?::((?:[^}]|}(?=}))*))?" //   :regex (may contain } if followed by })
          + "\\}"; // {name} or {name:regex} -> groups 2,3

  private final Map<String, String> pathParams;
  private final Map<String, String> matrixParams;
  private final Map<String, String> queryParams;
  private final Map<String, String> fragmentParams;

  private final RegexEngine re;
  private final DominoCompiledPattern exprCompiled;
  private final RegexValidationMode validationMode;
  private UrlSplitUtil urlSplitUtil;

  /** Backward-compatible constructor: one map powers all components. */
  public UrlFormatter(Map<String, String> params) {
    this(params, params, params, params);
  }

  /** Full constructor: separate maps per component. */
  public UrlFormatter(
      Map<String, String> pathParams,
      Map<String, String> matrixParams,
      Map<String, String> queryParams,
      Map<String, String> fragmentParams) {

    this.pathParams = pathParams;
    this.matrixParams = matrixParams;
    this.queryParams = queryParams;
    this.fragmentParams = fragmentParams;

    RestConfig cfg = DominoRestContext.make().getConfig();
    this.re = cfg.getRegexEngine();
    urlSplitUtil = new UrlSplitUtil(re);
    // We want to iterate all matches → compile with global flag where applicable.
    this.exprCompiled = re.compile(EXPR_PATTERN, "g");
    this.validationMode = DominoRestContext.make().getConfig().getRegexValidationMode();
  }

  /**
   * Formats a URL by replacing expressions in the token part (as defined by {@link
   * RestConfig#getRegexEngine()} ) and normalizing the result.
   *
   * @throws IllegalArgumentException if the url is null, or a {name:regex} has an invalid regex
   * @throws PathParameterMissingException if a required expression is missing in its component map
   */
  public String formatUrl(String targetUrl) {
    if (isNull(targetUrl)) {
      throw new IllegalArgumentException("URL cannot be null!.");
    }
    String trimmed = targetUrl.trim();
    if (trimmed.isEmpty()) {
      return "";
    }
    if (!hasExpressions(trimmed)) {
      return trimmed;
    }

    UrlSplitUtil.Split result = urlSplitUtil.split(trimmed);
    // 1) Extract the token (the part we should modify) and the untouched prefix.
    String postfix = result.rightSide;
    String prefix = result.leftSide;

    // 2) Split token into [path(+matrix)] [?query] [#fragment].
    String pathWithMatrix;
    String query = "";
    String fragment = "";

    // First, find '#' outside braces (fragment separator)
    int hashIdx = UrlSplitUtil.indexOfOutsideBraces(postfix, '#');
    if (hashIdx >= 0) {
      fragment = postfix.substring(hashIdx + 1);
      postfix = postfix.substring(0, hashIdx);
    }

    // Then find '?' outside braces (query separator) in the remaining part
    int qIdx = UrlSplitUtil.indexOfOutsideBraces(postfix, '?');
    if (qIdx >= 0) {
      query = postfix.substring(qIdx + 1);
      pathWithMatrix = postfix.substring(0, qIdx);
    } else {
      pathWithMatrix = postfix;
    }

    // 3) Replace per-component with its own map (+ regex validation).
    String replacedPathWithMatrix =
        replaceInPathAndMatrix(pathWithMatrix, pathParams, matrixParams);
    String replacedQuery = query.isEmpty() ? "" : replaceAll(query, queryParams, "query");
    String replacedFragment =
        fragment.isEmpty() ? "" : replaceAll(fragment, fragmentParams, "fragment");

    // 4) Reassemble token, normalize via ServicePath, and prepend prefix.
    String rebuiltToken =
        replacedPathWithMatrix
            + (replacedQuery.isEmpty() ? "" : "?" + replacedQuery)
            + (replacedFragment.isEmpty() ? "" : "#" + replacedFragment);

    ServicePath normalized = new ServicePath(rebuiltToken);
    return prefix + normalized.value();
  }

  private boolean hasExpressions(String url) {
    // quick heuristic — real replacement happens only inside the token via replaceAll
    return (url.contains("{") && url.contains("}")) || url.contains(":");
  }

  /**
   * Replaces expressions within the PATH portion, distinguishing:
   *
   * <ul>
   *   <li>segment name (uses pathMap)
   *   <li>matrix tail (everything after the first ';' in a segment, uses matrixMap)
   * </ul>
   */
  private String replaceInPathAndMatrix(
      String pathWithMatrix, Map<String, String> pathMap, Map<String, String> matrixMap) {

    if (pathWithMatrix.isEmpty()) return pathWithMatrix;

    // Keep empty segments so duplicate slashes are preserved before ServicePath normalization.
    String[] rawSegments = pathWithMatrix.split("/", -1);
    StringBuilder out = new StringBuilder(pathWithMatrix.length() + 16);

    for (int i = 0; i < rawSegments.length; i++) {
      String seg = rawSegments[i];
      if (i > 0) out.append('/');

      if (seg.isEmpty()) continue; // empty segment (e.g., multiple '/')

      int semi = seg.indexOf(';');
      String name = semi >= 0 ? seg.substring(0, semi) : seg;
      String matrixTail = semi >= 0 ? seg.substring(semi + 1) : "";

      String replacedName = replaceAll(name, pathMap, "path");
      String replacedTail = matrixTail.isEmpty() ? "" : replaceAll(matrixTail, matrixMap, "matrix");

      out.append(replacedName);
      if (!replacedTail.isEmpty()) {
        out.append(';').append(replacedTail);
      }
    }
    return out.toString();
  }

  /**
   * Replace every occurrence of <code>:name</code>, <code>{name}</code>, or <code>{name:regex}
   * </code> in {@code input} using {@code map}. For the regex form, the value MUST fully match the
   * regex.
   */
  private String replaceAll(String input, Map<String, String> map, String contextName) {
    if (input == null || input.isEmpty()) {
      return input;
    }

    StringBuilder out = new StringBuilder(input.length() + 16);
    int i = 0;
    int len = input.length();

    while (i < len) {
      char c = input.charAt(i);

      // ------------------------------------------------------------------
      // 1) Brace-style expressions: {name} or {name:regex}
      // ------------------------------------------------------------------
      if (c == '{') {
        int end = findExpressionEnd(input, i);
        if (end < 0) {
          // No closing '}' found; treat '{' as a normal char
          out.append(c);
          i++;
          continue;
        }

        String token = input.substring(i, end + 1); // inclusive
        ParsedExpression expr = ParsedExpression.parse(token);

        String value = map.get(expr.name);
        if (value == null) {
          throw new PathParameterMissingException(token + " (" + contextName + ")");
        }

        if (expr.regex != null && validationMode != RegexValidationMode.IGNORE) {
          boolean ok;
          try {
            ok = re.matches(expr.regex, value);
          } catch (RuntimeException ex) {
            // Invalid regex definition is still considered a hard error
            throw new IllegalArgumentException(
                "Invalid regex in " + contextName + " expression " + token + ": " + ex.getMessage(),
                ex);
          }

          if (!ok) {
            String msg =
                "Value '"
                    + value
                    + "' for "
                    + token
                    + " does not match its regex in "
                    + contextName
                    + " (mode="
                    + validationMode
                    + ")";

            if (validationMode == RegexValidationMode.FAIL) {
              throw new IllegalArgumentException(msg);
            } else if (validationMode == RegexValidationMode.WARN) {
              LOGGER.warning(msg);
              // fall through: still replace with value
            }
          }
        }
        out.append(value);
        i = end + 1;
        continue;
      }

      // ------------------------------------------------------------------
      // 2) Colon-style expressions: :name
      //    (only when ':' is followed by a valid name char)
      // ------------------------------------------------------------------
      if (c == ':') {
        int start = i;
        int j = i + 1;

        // Collect the name part [A-Za-z0-9_.-]+
        while (j < len && isNameChar(input.charAt(j))) {
          j++;
        }

        // If no valid name characters after ':', just treat ':' as literal
        if (j == i + 1) {
          out.append(c);
          i++;
          continue;
        }

        String token = input.substring(start, j); // e.g. ":b"
        ParsedExpression expr = ParsedExpression.parse(token);

        String value = map.get(expr.name);
        if (value == null) {
          throw new PathParameterMissingException(token + " (" + contextName + ")");
        }

        // Colon-style never has a regex (ParsedExpression.regex will be null)
        out.append(value);
        i = j;
        continue;
      }

      // ------------------------------------------------------------------
      // 3) Anything else: copy as-is
      // ------------------------------------------------------------------
      out.append(c);
      i++;
    }

    return out.toString();
  }

  /**
   * Finds the index of the '}' that closes a {name:regex} expression starting at 'startIdx'.
   * Supports internal {m,n} quantifiers inside the regex.
   *
   * <p>Returns -1 if no matching '}' is found.
   */
  private int findExpressionEnd(String input, int startIdx) {
    int len = input.length();
    if (startIdx >= len || input.charAt(startIdx) != '{') {
      return -1;
    }

    int depth = 0;
    for (int i = startIdx; i < len; i++) {
      char ch = input.charAt(i);
      if (ch == '{') {
        if (i == startIdx) {
          // This is the outer placeholder '{'
          continue;
        }
        depth++; // internal '{' (e.g., in {8,12})
      } else if (ch == '}') {
        if (depth > 0) {
          depth--; // closing an internal '{...}'
        } else {
          // depth == 0 and this is the first '}' after startIdx that isn't closing an internal
          // brace
          return i;
        }
      }
    }
    return -1; // no closing '}' found
  }

  private static final class ParsedExpression {
    final String name;
    final String regex; // may be null if no regex is specified

    private ParsedExpression(String name, String regex) {
      this.name = name;
      this.regex = regex;
    }

    static ParsedExpression parse(String token) {
      if (token == null) {
        throw new IllegalArgumentException("Expression token cannot be null");
      }

      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        throw new IllegalArgumentException("Invalid empty expression token");
      }

      boolean hasBraces = trimmed.startsWith("{") && trimmed.endsWith("}");
      String inner = hasBraces ? trimmed.substring(1, trimmed.length() - 1).trim() : trimmed;

      if (inner.isEmpty()) {
        throw new IllegalArgumentException("Invalid expression token: " + token);
      }

      // Support ":name" style (leading colon, no second ':')
      if (inner.charAt(0) == ':' && inner.indexOf(':', 1) < 0) {
        String name = inner.substring(1).trim();
        if (name.isEmpty()) {
          throw new IllegalArgumentException("Empty parameter name in expression: " + token);
        }
        return new ParsedExpression(name, null);
      }

      int colonIdx = inner.indexOf(':');

      if (colonIdx < 0) {
        // "{name}" or "name" → name only, no regex
        String name = inner.trim();
        if (name.isEmpty()) {
          throw new IllegalArgumentException("Empty parameter name in expression: " + token);
        }
        return new ParsedExpression(name, null);
      }

      // Handle "name:" (no regex), "name:regex", or even "name: regex with spaces"
      String name = inner.substring(0, colonIdx).trim();
      String regex = inner.substring(colonIdx + 1).trim();

      if (name.isEmpty()) {
        throw new IllegalArgumentException("Empty parameter name in expression: " + token);
      }
      if (regex.isEmpty()) {
        // Interpret "name:" as "name with no regex constraint"
        regex = null;
      }

      return new ParsedExpression(name, regex);
    }
  }

  private boolean isNameChar(char ch) {
    return (ch >= 'A' && ch <= 'Z')
        || (ch >= 'a' && ch <= 'z')
        || (ch >= '0' && ch <= '9')
        || ch == '_'
        || ch == '.'
        || ch == '-';
  }
}

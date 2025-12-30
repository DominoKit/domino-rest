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

import org.dominokit.rest.shared.regex.DominoCompiledPattern;
import org.dominokit.rest.shared.regex.DominoMatcher;
import org.dominokit.rest.shared.regex.RegexEngine;

/** Utility class for splitting and parsing URLs into segments. */
public final class UrlSplitUtil {

  // Matches: [scheme: ]// [optional userinfo@] [host:port] [rest...]
  // Groups:
  //   1 = scheme (may be empty for protocol-relative URLs)
  //   2 = host:port (authority w/o userinfo)
  //   3 = rest (path?query#fragment, may be empty)
  private static final String SPLIT_PATTERN =
      "^(?:(?:([A-Za-z][A-Za-z0-9+.-]*):)?//)(?:[^@/?#]*@)?([^/?#]+)(.*)$";

  private final DominoCompiledPattern compiled;

  public UrlSplitUtil(RegexEngine engine) {
    this.compiled = engine.compile(SPLIT_PATTERN);
  }

  /**
   * Result: base = scheme://host[:port], rest = remainder (may be empty).
   *
   * @param url the URL to split
   * @return the split result
   */
  public Split split(String url) {
    DominoMatcher m = compiled.matcher(url);
    if (m.isMatch()) {
      String scheme = nullToEmpty(m.group(1)); // may be ""
      String hostport = m.group(2); // never null on match
      String rest = nullToEmpty(m.group(3)); // possibly ""
      String base = (scheme.isEmpty() ? "" : scheme + ":") + "//" + hostport;
      return new Split(base, rest);
    }
    // No scheme//authority → treat entire input as "rest"
    return new Split("", url);
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  /**
   * Finds the index of the first occurrence of {@code target} in {@code token} that is <b>not</b>
   * inside a { ... } expression.
   *
   * <p>Nested braces (e.g. quantifiers like {8,12} inside the regex) are handled by tracking depth.
   *
   * @param token the string to search in
   * @param target the character to find
   * @return the index of the target character, or -1 if not found
   */
  public static int indexOfOutsideBraces(String token, char target) {
    if (token == null || token.isEmpty()) {
      return -1;
    }
    int depth = 0;
    for (int i = 0; i < token.length(); i++) {
      char c = token.charAt(i);
      if (c == '{') {
        depth++;
      } else if (c == '}') {
        if (depth > 0) {
          depth--;
        }
      }
      if (c == target && depth == 0) {
        return i;
      }
    }
    return -1;
  }

  public static final class Split {
    public final String leftSide;
    public final String rightSide;

    public Split(String leftSide, String rightSide) {
      this.leftSide = leftSide;
      this.rightSide = rightSide;
    }
  }
}

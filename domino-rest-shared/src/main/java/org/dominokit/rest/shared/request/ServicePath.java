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
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dominokit.rest.shared.regex.DominoCompiledPattern;
import org.dominokit.rest.shared.regex.RegexEngine;

/**
 * Parses and manipulates a URL-like token into path segments, query parameters, fragments, and
 * (now) per-segment <b>matrix parameters</b>. A matrix parameter is attached to a path segment,
 * e.g. {@code /users;region=eu/42;active=true}.
 *
 * <p><strong>Compatibility:</strong>
 *
 * <ul>
 *   <li>{@link #paths()} still returns only the plain segment names (without matrix parameters).
 *   <li>{@link #path()} historically returned {@code a/b/c}. It now renders the <emfull path with
 *       matrix parameters</em> (to keep round-tripping intact). If you need the name-only version,
 *       use {@link #pathWithoutMatrix()}.
 * </ul>
 *
 * <p><strong>Encoding:</strong> This class does not perform URL encode/decode. Values are taken
 * verbatim, consistent with previous behavior.
 */
public class ServicePath {

  private static final String QUERY_REGEX = "\\?";
  private static final String FRAGMENT_REGEX = "\\#";

  /** Root path that is logically prefixed (but not necessarily present in the token). */
  private final String rootPath;

  /**
   * Back-compat cache of segment names. Always derived from {@link #pathSegments}. Do not modify
   * directly.
   */
  private List<String> paths = new LinkedList<>();

  private List<Parameter> queryParameters = new LinkedList<>();
  private List<String> fragments = new LinkedList<>();

  /** Structured list of path segments, each with its own matrix parameters. */
  private List<PathSegment> pathSegments = new LinkedList<>();

  // Remember if the (rebased) token started with a leading slash.
  private final boolean leadingSlash;

  // ---------------------------------------------------------------------------------------------
  // Constructors
  // ---------------------------------------------------------------------------------------------

  /**
   * Creates a {@link ServicePath} from a URL token.
   *
   * @param token a URL token (e.g. {@code /a/b;c=1?x=1#frag})
   * @throws IllegalArgumentException if {@code token} is null
   */
  public ServicePath(String token) {
    this("", token);
  }

  /**
   * Creates a {@link ServicePath} with an explicit root path used for re-basing.
   *
   * <p>If {@code token} starts with {@code rootPath}, the root portion is stripped from the token
   * before parsing.
   *
   * @param rootPath the root path token (may be {@code null} or empty)
   * @param token a URL token (e.g. {@code /a/b;c=1?x=1#frag})
   * @throws IllegalArgumentException if {@code token} is null
   */
  public ServicePath(String rootPath, String token) {
    if (isNull(token)) throw new IllegalArgumentException();
    this.rootPath = isNull(rootPath) ? "" : rootPath.trim();
    String rebasedToken = rebaseToken(rootPath, token);
    this.leadingSlash = rebasedToken.startsWith("/");
    this.pathSegments.addAll(asPathSegments(rebasedToken));
    this.paths =
        this.pathSegments.stream()
            .map(PathSegment::name)
            .collect(Collectors.toCollection(LinkedList::new));
    this.queryParameters.addAll(asQueryParameters(rebasedToken));
    this.fragments.addAll(parseFragments(rebasedToken));
  }

  /** If rootPath is a non-empty prefix of token, remove it; otherwise return token unchanged. */
  private String rebaseToken(String rootPath, String token) {
    if (isNull(rootPath) || rootPath.trim().isEmpty() || !token.startsWith(rootPath)) {
      return token;
    }
    return token.substring(rootPath.length());
  }

  /** Parses fragments from a token. Fragments are read after the '#' and then split by '/'. */
  private List<String> parseFragments(String token) {
    int first = token.indexOf('#');
    if (first >= 0 && first < token.length() - 1) {
      String frag = token.substring(first + 1);

      // If someone used multiple hashes in a row (e.g., "##frag/child"), strip the extra leading
      // '#'
      int i = 0;
      while (i < frag.length() && frag.charAt(i) == '#') i++;
      String normalized = frag.substring(i);

      if (!normalized.isEmpty()) {
        // IMPORTANT: Do NOT call asNamesList()/splittedPaths() here
        // because they strip '#' again. Just split the fragment by '/'.
        String[] parts = normalized.split("/");
        java.util.LinkedList<String> list = new java.util.LinkedList<>();
        for (String p : parts) {
          if (!p.isEmpty()) list.add(p);
        }
        return list;
      }
    }
    return new java.util.LinkedList<>();
  }

  // ---------------------------------------------------------------------------------------------
  // Path matching / accessors
  // ---------------------------------------------------------------------------------------------

  /**
   * Returns whether the <em>path portion</em> ends with the given sequence of segments. Matrix
   * parameters are ignored for this check.
   *
   * @param path a path string such as {@code "a/b"} (no query/fragment)
   * @return true if the current path ends with the given sequence; false otherwise
   */
  public boolean endsWithPath(String path) {
    if (isEmpty(path)) return false;
    return endsWith(paths(), asNamesList(path));
  }

  private boolean endsWith(List<String> paths, List<String> targets) {
    if (isValidSize(paths, targets)) return matchEnds(paths, targets);
    return false;
  }

  private boolean matchEnds(List<String> paths, List<String> targets) {
    int offset = paths.size() - targets.size();
    return IntStream.range(0, targets.size())
        .allMatch(i -> targets.get(i).equals(paths.get(i + offset)));
  }

  /**
   * @return a list of segment <em>names</em> (matrix parameters are not included). For {@code
   *     http://localhost:8080/a;b=1/c;d=2}, returns {@code ["a", "c"]}.
   */
  public List<String> paths() {
    return paths;
  }

  /**
   * @return the parsed fragments (split by '/'). For {@code http://x/a#d/e/f}, returns {@code
   *     ["d","e","f"]}.
   */
  public List<String> fragments() {
    return fragments;
  }

  public ServicePath appendFragment(String fragment) {
    this.fragments.add(fragment);
    return this;
  }

  public boolean hasFragment(String fragment) {
    return this.fragments.contains(fragment);
  }

  /**
   * @return the full path string including any <em>matrix parameters</em>, e.g. {@code
   *     a;b=1/c;d=2}. If you need names only, use {@link #pathWithoutMatrix()}.
   */
  public String path() {
    return pathWithMatrix();
  }

  /**
   * @return the path composed of <em>segment names only</em> (no matrix parameters), joined by '/'.
   */
  public String pathWithoutMatrix() {
    return String.join("/", paths());
  }

  /** Renders the path including matrix parameters. */
  private String pathWithMatrix() {
    return pathSegments.stream().map(PathSegment::asPathString).collect(Collectors.joining("/"));
  }

  // ---------------------------------------------------------------------------------------------
  // Query parameters
  // ---------------------------------------------------------------------------------------------

  /** @return the full query string (without the leading '?'), e.g. {@code x=1&y=2&y=3} */
  public String query() {
    return queryParameters.stream().map(Parameter::asQueryString).collect(Collectors.joining("&"));
  }

  /**
   * Checks if a query parameter with the given name exists.
   *
   * @param name parameter name
   * @return true if present; false otherwise
   */
  public boolean hasQueryParameter(String name) {
    return queryParameters.stream().anyMatch(parameter -> parameter.key.equals(name));
  }

  /**
   * Sets a query parameter to a single value, replacing any existing values for that name.
   *
   * @param name parameter name
   * @param value parameter value
   * @return this
   */
  public ServicePath setQueryParameter(String name, String value) {
    return setQueryParameter(name, asList(value));
  }

  /**
   * Sets a query parameter to multiple values, replacing any existing values for that name.
   *
   * @param name parameter name
   * @param values parameter values
   * @return this
   */
  public ServicePath setQueryParameter(String name, List<String> values) {
    if (isNull(name) || name.trim().isEmpty()) return this;

    int idx = parameterIndex(name);
    if (idx >= 0) {
      queryParameters.get(idx).setValues(values); // replace in place (keep order)
    } else {
      queryParameters.add(new Parameter(name, new LinkedList<>(values))); // new at end
    }
    return this;
  }

  private Parameter getParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();
    return param.orElse(null);
  }

  /**
   * Appends an additional query parameter occurrence at the end of the query string.
   *
   * @param name parameter name
   * @param value parameter value
   * @return this
   */
  public ServicePath appendParameter(String name, String value) {
    return appendParameter(name, asList(value));
  }

  /**
   * Appends additional query parameter occurrences at the end of the query string.
   *
   * @param name parameter name
   * @param values values to append for the given name
   * @return this
   */
  public ServicePath appendParameter(String name, List<String> values) {
    if (nonNull(name) && !name.trim().isEmpty()) {
      if (hasQueryParameter(name)) {
        getParameter(name).addValues(values);
      } else {
        this.queryParameters.add(new Parameter(name, values));
      }
    }
    return this;
  }

  private int parameterIndex(String name) {
    for (int i = 0; i < queryParameters.size(); i++) {
      if (queryParameters.get(i).key.equals(name)) return i;
    }
    return -1;
  }

  /**
   * Replaces the first occurrence of a path segment name with a new name.
   *
   * <p>Only the segment <em>name</em> is changed; any matrix parameters attached to that segment
   * are preserved. If the name does not exist, this is a no-op.
   *
   * @param path the segment name to be replaced (compared to {@link PathSegment#name()})
   * @param replacement the new segment name
   * @return this {@link ServicePath}
   */
  public ServicePath replacePath(String path, String replacement) {
    if (isNull(path) || isNull(replacement)) {
      return this;
    }
    for (int i = 0; i < pathSegments.size(); i++) {
      PathSegment seg = pathSegments.get(i);
      if (Objects.equals(seg.name(), path)) {
        pathSegments.set(i, seg.renamed(replacement));
        refreshPathsCache();
        break; // only the first occurrence
      }
    }
    return this;
  }

  /**
   * Replaces the segment name at the given index.
   *
   * @param index zero-based segment index
   * @param replacement new segment name
   * @return this {@link ServicePath}
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public ServicePath replacePathAt(int index, String replacement) {
    PathSegment seg = pathSegments.get(index);
    pathSegments.set(index, seg.renamed(replacement));
    refreshPathsCache();
    return this;
  }

  /**
   * Removes a query parameter (all occurrences) by name.
   *
   * @param name parameter name
   * @return this
   */
  public ServicePath removeParameter(String name) {
    Parameter parameter = getParameter(name);
    if (nonNull(parameter)) {
      this.queryParameters.remove(parameter);
    }
    return this;
  }

  // ---------------------------------------------------------------------------------------------
  // Matrix parameters (NEW)
  // ---------------------------------------------------------------------------------------------

  /**
   * @return immutable-like view of structured path segments (name + matrix params). Mutations must
   *     go through {@code set/append/removeMatrixParameter} methods.
   */
  public List<PathSegment> pathSegments() {
    return pathSegments;
  }

  /**
   * Checks if a given segment has a matrix parameter with the provided name.
   *
   * @param segmentIndex zero-based index of the segment in {@link #pathSegments}
   * @param name matrix parameter name
   * @return true if present; false otherwise
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public boolean hasMatrixParameter(int segmentIndex, String name) {
    return pathSegments.get(segmentIndex).hasMatrixParameter(name);
  }

  /**
   * Reads all values for a matrix parameter on the specified segment.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @return list of values, or empty list if not present
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public List<String> matrixParameterValues(int segmentIndex, String name) {
    return pathSegments.get(segmentIndex).matrixValues(name);
  }

  /**
   * Replaces all values of the named matrix parameter on a segment with a single value.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @param value new value
   * @return this
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public ServicePath setMatrixParameter(int segmentIndex, String name, String value) {
    return setMatrixParameter(segmentIndex, name, asList(value));
  }

  /**
   * Replaces all values of the named matrix parameter on a segment with the provided values.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @param values new values
   * @return this
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public ServicePath setMatrixParameter(int segmentIndex, String name, List<String> values) {
    pathSegments.get(segmentIndex).setMatrix(name, values);
    refreshPathsCache();
    return this;
  }

  /**
   * Appends additional values for a named matrix parameter on a segment.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @param value value to append
   * @return this
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public ServicePath appendMatrixParameter(int segmentIndex, String name, String value) {
    pathSegments.get(segmentIndex).appendMatrix(name, asList(value));
    refreshPathsCache();
    return this;
  }

  /**
   * Appends additional values for a named matrix parameter on a segment.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @param values values to append
   * @return this
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public ServicePath appendMatrixParameter(int segmentIndex, String name, List<String> values) {
    pathSegments.get(segmentIndex).appendMatrix(name, values);
    refreshPathsCache();
    return this;
  }

  /**
   * Removes an entire matrix parameter (all values) from a segment if present.
   *
   * @param segmentIndex zero-based index of the segment
   * @param name parameter name
   * @return this
   * @throws IndexOutOfBoundsException if {@code segmentIndex} is invalid
   */
  public ServicePath removeMatrixParameter(int segmentIndex, String name) {
    pathSegments.get(segmentIndex).removeMatrix(name);
    refreshPathsCache();
    return this;
  }

  private void refreshPathsCache() {
    this.paths =
        this.pathSegments.stream()
            .map(PathSegment::name)
            .collect(Collectors.toCollection(LinkedList::new));
  }

  // ---------------------------------------------------------------------------------------------
  // Fragment & general state
  // ---------------------------------------------------------------------------------------------

  /** @return the fragment portion (without leading '#'), joined by '/'. */
  public String fragment() {
    return String.join("/", fragments());
  }

  /** @return true if all parts (path, query, fragments) are empty. */
  public boolean isEmpty() {
    return paths.isEmpty() && queryParameters.isEmpty() && fragments.isEmpty();
  }

  /**
   * @return the full string representation including root path (when applicable), path (with matrix
   *     params), query, and fragment.
   */
  public String value() {
    String body = noRootValue();
    String separator =
        (getRootPath().isEmpty()
                || getRootPath().endsWith("/")
                || body.startsWith("/")
                || body.isEmpty())
            ? ""
            : "/";
    return getRootPath() + separator + body;
  }

  private UrlTokenRegexMatcher getUrlTokenRegexMatcher() {
    RegexEngine re = DominoRestContext.make().getConfig().getRegexEngine();
    // Compile once (no special flags needed)
    final DominoCompiledPattern hostAndPath =
        re.compile("^((.*:)//([a-z0-9\\-.]+)(|:[0-9]+)/)(.*)$");

    return url -> {
      // keep your fast pre-check
      if (url.contains("http:") || url.contains("https:")) {
        // Full match check keeps replaceAll logic simple and cross-platform
        if (re.matches(hostAndPath.pattern(), url, hostAndPath.flags())) {
          // Replace whole match with group(5) – the path/token portion
          return re.replaceAll(url, hostAndPath, m -> m.group(5));
        }
      }
      return url;
    };
  }

  /**
   * @return the full string representation excluding the root path: {@code
   *     <path-with-matrix>?<query>#<fragment>}
   */
  public String noRootValue() {
    String p = pathWithMatrix();
    if (leadingSlash && !p.isEmpty() && !p.startsWith("/")) {
      p = "/" + p;
    }
    return p + appendQuery(query()) + appendFragment();
  }

  private String appendFragment() {
    return isEmpty(fragment()) ? "" : "#" + fragment();
  }

  private String appendQuery(String query) {
    return isEmpty(query) ? "" : "?" + query;
  }

  // ---------------------------------------------------------------------------------------------
  // Path parsing helpers
  // ---------------------------------------------------------------------------------------------

  private List<String> asNamesList(String token) {
    if (isNull(token) || isEmpty(token) || token.startsWith("?") || token.startsWith("#"))
      return new ArrayList<>();
    return Arrays.stream(splittedPaths(token))
        .filter(p -> !p.isEmpty())
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private String[] splittedPaths(String pathString) {
    return parsePathPart(pathString).split("/");
  }

  private String parsePathPart(String pathString) {
    if (pathString == null) {
      return "";
    }
    String cleaned = pathString.replace("!", "");

    // Strip fragment (first '#' outside braces)
    int hashIdx = UrlSplitUtil.indexOfOutsideBraces(cleaned, '#');
    if (hashIdx >= 0) {
      cleaned = cleaned.substring(0, hashIdx);
    }

    // Strip query (first '?' outside braces, before fragment)
    int qIdx = UrlSplitUtil.indexOfOutsideBraces(cleaned, '?');
    if (qIdx >= 0) {
      cleaned = cleaned.substring(0, qIdx);
    }

    return cleaned;
  }

  private boolean isEmpty(String path) {
    return isNull(path) || path.isEmpty();
  }

  private boolean isValidSize(List<String> paths, List<String> targets) {
    return !targets.isEmpty() && targets.size() <= paths.size();
  }

  // ---------------------------------------------------------------------------------------------
  // Query parsing
  // ---------------------------------------------------------------------------------------------

  private List<Parameter> asQueryParameters(String token) {
    String queryString = queryPart(token);
    if (isNull(queryString) || queryString.trim().isEmpty()) {
      return new LinkedList<>();
    }
    return parsedParameters(queryString, "&", false);
  }

  // Replace the old parsedParameters(String, String) with this:
  private List<Parameter> parsedParameters(
      String paramString, String delimiter, boolean allowFlags) {
    return Stream.of(paramString.split(delimiter)).map(part -> part.split("=", 2))
        .collect(
            Collectors.groupingBy(
                keyValue -> keyValue[0],
                LinkedHashMap::new,
                Collectors.mapping(
                    keyValue -> {
                      if (keyValue.length > 1) {
                        return keyValue[1]; // has explicit value
                      } else {
                        return allowFlags ? null : ""; // flag vs empty
                      }
                    },
                    Collectors.toList())))
        .entrySet().stream()
        .map(entry -> new Parameter(entry.getKey(), entry.getValue()))
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private String queryPart(String token) {
    if (token == null) {
      return "";
    }
    String cleaned = token.replace("!", "");

    // First, strip fragment (anything after '#' outside braces)
    int hashIdx = UrlSplitUtil.indexOfOutsideBraces(cleaned, '#');
    String beforeFragment = hashIdx >= 0 ? cleaned.substring(0, hashIdx) : cleaned;

    // Then find '?' outside braces
    int qIdx = UrlSplitUtil.indexOfOutsideBraces(beforeFragment, '?');
    if (qIdx < 0 || qIdx == beforeFragment.length() - 1) {
      return "";
    }

    String query = beforeFragment.substring(qIdx + 1);

    if (!query.isEmpty() && !query.contains("=")) {
      throw new IllegalArgumentException("Query string [" + query + "] is missing '=' operator.");
    }
    return query;
  }

  // ---------------------------------------------------------------------------------------------
  // Path segments with matrix parsing (NEW)
  // ---------------------------------------------------------------------------------------------

  private List<PathSegment> asPathSegments(String token) {
    String[] rawSegments = splittedPaths(token);
    LinkedList<PathSegment> result = new LinkedList<>();
    for (String raw : rawSegments) {
      if (raw.isEmpty()) continue;
      // Split by ';' -> first token is the name, rest are matrix parts "k=v" (k or v may be empty)
      String[] parts = raw.split(";");
      String name = parts.length == 0 ? "" : parts[0];
      List<Parameter> matrix = new LinkedList<>();
      if (parts.length > 1) {
        String matrixString = Arrays.stream(parts).skip(1).collect(Collectors.joining(";"));
        if (!matrixString.isEmpty()) {
          matrix.addAll(parsedParameters(matrixString, ";", true));
        }
      }
      result.add(new PathSegment(name, matrix));
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // Accessors and equality
  // ---------------------------------------------------------------------------------------------

  /** @return the configured root path (may be empty, never null). */
  public String getRootPath() {
    return rootPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServicePath)) return false;
    ServicePath that = (ServicePath) o;

    return paths.equals(that.paths)
        && fragments.equals(that.fragments)
        && queryParameters.size() == that.queryParameters.size()
        && queryParameters.containsAll(that.queryParameters)
        && pathSegments.equals(that.pathSegments);
  }

  // inside ServicePath
  @Override
  public int hashCode() {
    return 31 * Objects.hash(paths, fragments, pathSegments)
        + orderInsensitiveParamsHash(queryParameters);
  }

  private int orderInsensitiveParamsHash(List<Parameter> params) {
    List<Parameter> copy = new ArrayList<>(params);
    copy.sort(
        (a, b) -> {
          int c = a.key.compareTo(b.key);
          if (c != 0) return c;
          List<String> av = new ArrayList<>(a.value);
          List<String> bv = new ArrayList<>(b.value);
          av.sort(Comparator.nullsFirst(String::compareTo));
          bv.sort(Comparator.nullsFirst(String::compareTo));
          return av.toString().compareTo(bv.toString());
        });
    return copy.hashCode();
  }

  @Override
  public String toString() {
    // Full round-trippable representation including root, query, and fragment
    return "ServicePath{" + value() + "}";
  }

  // ---------------------------------------------------------------------------------------------
  // Internal types
  // ---------------------------------------------------------------------------------------------

  /**
   * Name + values container used for query and matrix parameters. Equality ignores order of values
   * but considers multiplicity.
   */
  private static class Parameter {
    private String key;
    private List<String> value;

    Parameter(String key, List<String> value) {
      this.key = key;
      this.value = new LinkedList<>(value);
    }

    void addValues(List<String> moreValues) {
      value.addAll(moreValues);
    }

    void setValues(List<String> newValues) {
      this.value.clear();
      this.value.addAll(newValues);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Parameter)) return false;
      Parameter parameter = (Parameter) o;
      return Objects.equals(key, parameter.key)
          && value.size() == parameter.value.size()
          && value.containsAll(parameter.value);
    }

    // inside ServicePath.Parameter
    @Override
    public int hashCode() {
      List<String> copy = new ArrayList<>(value);
      copy.sort(Comparator.nullsFirst(String::compareTo));
      return Objects.hash(key, copy);
    }

    String asQueryString() {
      return value.stream().map(v -> key + "=" + v).collect(Collectors.joining("&"));
    }

    String asMatrixString() {
      return value.stream()
          .map(v -> v == null ? key : (key + "=" + v))
          .collect(Collectors.joining(";"));
    }
  }

  /**
   * Represents a single path segment with its matrix parameters. For example, {@code
   * users;region=eu;active=true}.
   */
  public static class PathSegment {
    private final String name;
    private final LinkedList<Parameter> matrix;

    PathSegment(String name, List<Parameter> matrix) {
      this.name = name == null ? "" : name;
      this.matrix = new LinkedList<>(matrix == null ? new LinkedList<>() : matrix);
    }

    /** @return segment name (without matrix parameters) */
    public String name() {
      return name;
    }

    /** @return true if a matrix parameter with the given name exists */
    public boolean hasMatrixParameter(String param) {
      return matrix.stream().anyMatch(p -> p.key.equals(param));
    }

    /** @return all values for a matrix parameter (empty list if not present) */
    public List<String> matrixValues(String param) {
      return matrix.stream()
          .filter(p -> p.key.equals(param))
          .findFirst()
          .map(p -> new LinkedList<>(p.value))
          .orElseGet(LinkedList::new);
    }

    /** Replace all values for a matrix parameter with the given values (creating it if absent). */
    void setMatrix(String param, List<String> values) {
      Parameter current = matrix.stream().filter(p -> p.key.equals(param)).findFirst().orElse(null);
      if (current == null) {
        matrix.add(new Parameter(param, values));
      } else {
        current.value.clear();
        current.value.addAll(values);
      }
    }

    /** Append additional values to the named matrix parameter (creating it if absent). */
    void appendMatrix(String param, List<String> values) {
      Parameter current = matrix.stream().filter(p -> p.key.equals(param)).findFirst().orElse(null);
      if (current == null) {
        matrix.add(new Parameter(param, values));
      } else {
        current.addValues(values);
      }
    }

    /** Remove the named matrix parameter entirely (if present). */
    void removeMatrix(String param) {
      Parameter current = matrix.stream().filter(p -> p.key.equals(param)).findFirst().orElse(null);
      if (current != null) {
        matrix.remove(current);
      }
    }

    /**
     * Render as a path segment including matrix parameters, e.g. {@code
     * users;active=true;role=admin}.
     */
    String asPathString() {
      if (matrix.isEmpty()) return name;
      String matrices =
          matrix.stream().map(Parameter::asMatrixString).collect(Collectors.joining(";"));
      return name + ";" + matrices;
    }

    /** @return a new {@link PathSegment} with the same matrix parameters and a different name. */
    PathSegment renamed(String newName) {
      return new PathSegment(newName, new LinkedList<>(this.matrix));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PathSegment)) return false;
      PathSegment that = (PathSegment) o;
      return Objects.equals(name, that.name)
          && matrix.size() == that.matrix.size()
          && matrix.containsAll(that.matrix);
    }

    // inside ServicePath.PathSegment
    @Override
    public int hashCode() {
      List<Parameter> copy = new ArrayList<>(matrix);
      copy.sort(
          (a, b) -> {
            int c = a.key.compareTo(b.key);
            if (c != 0) return c;
            List<String> av = new ArrayList<>(a.value);
            List<String> bv = new ArrayList<>(b.value);
            av.sort(Comparator.nullsFirst(String::compareTo));
            bv.sort(Comparator.nullsFirst(String::compareTo));
            return av.toString().compareTo(bv.toString());
          });
      return Objects.hash(name, copy);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Small helpers
  // ---------------------------------------------------------------------------------------------

  private List<String> asList(String value) {
    List<String> values = new ArrayList<>();
    values.add(value);
    return values;
  }
}

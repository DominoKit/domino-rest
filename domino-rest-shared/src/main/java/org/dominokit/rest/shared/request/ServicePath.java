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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ServicePath {

  private static final String QUERY_REGEX = "\\?";
  private static final String FRAGMENT_REGEX = "\\#";
  private final String rootPath;
  private List<String> paths = new LinkedList<>();
  private List<Parameter> queryParameters = new LinkedList<>();
  private List<String> fragments = new LinkedList<>();

  /** @param token String, a URL token */
  public ServicePath(String token) {
    this("", token);
  }

  /**
   * @param rootPath String, the root path token
   * @param token String, a URL token
   */
  public ServicePath(String rootPath, String token) {
    if (isNull(token)) throw new IllegalArgumentException();
    this.rootPath = isNull(rootPath) ? "" : rootPath.trim();
    String rebasedToken = rebaseToken(rootPath, token);
    this.paths.addAll(asPathsList(rebasedToken));
    this.queryParameters.addAll(asQueryParameters(rebasedToken));
    this.fragments.addAll(parseFragments(rebasedToken));
  }

  private String rebaseToken(String rootPath, String token) {
    if (isNull(rootPath) || rootPath.trim().isEmpty() || !token.startsWith(rootPath)) {
      return token;
    }
    return token.substring(rootPath.length());
  }

  private List<String> parseFragments(String token) {
    if (token.contains("#") && token.indexOf("#") < token.length() - 1)
      return asPathsList(token.split(FRAGMENT_REGEX)[1]);
    return new LinkedList<>();
  }

  /**
   * @param path The path to check for
   * @return <b>true</b> if the path part of the url ends with the specified path otherwise returns
   *     <b>false</b>
   */
  public boolean endsWithPath(String path) {
    if (isEmpty(path)) return false;
    return endsWith(paths(), asPathsList(path));
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
   * @return a list of Strings representing all paths of a url, e.g
   *     <b>http://localhost:8080/a/b/c</b> will return a list contains <b>a</b>, <b>b</b>,
   *     <b>c</b>,
   */
  public List<String> paths() {
    return paths;
  }

  /**
   * @return a list of Strings representing all fragments of a url, e.g
   *     <b>http://localhost:8080/a/b/c#d/e/f</b> will return a list contains <b>d</b>, <b>d</b>,
   *     <b>f</b>,
   */
  public List<String> fragments() {
    return fragments;
  }

  /**
   * @return the path part of a url, e.g <b>http://localhost:8080/a/b/c</b> will return <b>a/b/c</b>
   */
  public String path() {
    return String.join("/", paths());
  }

  /** @return the string representing the whole query part of a token */
  public String query() {
    return queryParameters.stream().map(Parameter::asQueryString).collect(Collectors.joining("&"));
  }

  /**
   * @param name name of the query parameter
   * @return <b>True</b> if the token has a query param that has the specified name, otherwise
   *     returns <b>false</b>.
   */
  public boolean hasQueryParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();

    if (param.isPresent()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds a query parameter with specified name and value to the current token, if a query parameter
   * with same name already exists, then replaces its value with the new one
   *
   * @param name query parameter name
   * @param value query parameter value
   * @return {@link ServicePath} that contains the new query parameter.
   */
  public ServicePath setQueryParameter(String name, String value) {
    if (hasQueryParameter(name)) {
      removeParameter(name);
    }
    appendParameter(name, value);
    return this;
  }

  /** {@inheritDoc} */
  public ServicePath setQueryParameter(String name, List<String> values) {
    if (hasQueryParameter(name)) {
      removeParameter(name);
    }
    appendParameter(name, values);
    return this;
  }

  private Parameter getParameter(String name) {
    Optional<Parameter> param =
        queryParameters.stream().filter(parameter -> parameter.key.equals(name)).findFirst();

    if (param.isPresent()) {
      return param.get();
    } else {
      return null;
    }
  }

  /**
   * Appends a new query parameter to the end of the token query parameters part.
   *
   * @param name of the query parameter
   * @param value of the query parameter
   * @return {@link ServicePath} with the new query parameter appended to the end of query part.
   */
  public ServicePath appendParameter(String name, String value) {
    return appendParameter(name, asList(value));
  }

  /** {@inheritDoc} */
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

  /**
   * Replaces the first occurrence of a path segment with the replacement
   *
   * @param path The path segment to be replaced
   * @param replacement the new path segment
   * @return {@link ServicePath} with path segment replaced by the replacement
   */
  public ServicePath replacePath(String path, String replacement) {
    List<String> paths = asPathsList(path());
    if (paths.contains(path)) {
      int i = paths.indexOf(path);
      paths.add(i, replacement);
      paths.remove(i + 1);
      this.paths = paths;
    }
    return this;
  }

  private List<String> asList(String value) {
    List<String> values = new ArrayList<>();
    values.add(value);
    return values;
  }

  /**
   * Removes the query parameter with the specified name
   *
   * @param name of the parameter to be removed
   * @return {@link ServicePath} with the query parameter with the specified name being removed
   */
  public ServicePath removeParameter(String name) {
    Parameter parameter = getParameter(name);
    if (nonNull(parameter)) {
      this.queryParameters.remove(parameter);
    }
    return this;
  }

  /** @return the string representing the whole fragment part of a token */
  public String fragment() {
    return String.join("/", fragments());
  }

  /**
   * @return <b>true</b> if all of token (path part, query part, fragments part) are empty,
   *     otherwise return <b>false</b>.
   */
  public boolean isEmpty() {
    return paths.isEmpty() && queryParameters.isEmpty() && fragments.isEmpty();
  }

  /** @return the full string representation of a {@link ServicePath} */
  public String value() {
    String path = path();
    String separator =
        (getRootPath().isEmpty()
                || getRootPath().endsWith("/")
                || path.startsWith("/")
                || path.isEmpty())
            ? ""
            : "/";
    return getRootPath() + separator + noRootValue();
  }

  /** {@inheritDoc} */
  public String noRootValue() {
    return path() + appendQuery(query()) + appendFragment();
  }

  private String appendFragment() {
    return isEmpty(fragment()) ? "" : "#" + fragment();
  }

  private String appendQuery(String query) {
    return isEmpty(query) ? "" : "?" + query;
  }

  private List<String> asPathsList(String token) {
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
    return pathString.replace("!", "").split(QUERY_REGEX)[0].split(FRAGMENT_REGEX)[0];
  }

  private boolean isEmpty(String path) {
    return isNull(path) || path.isEmpty();
  }

  private boolean isValidSize(List<String> paths, List<String> targets) {
    return !targets.isEmpty() && targets.size() <= paths.size();
  }

  private List<Parameter> asQueryParameters(String token) {

    String queryString = queryPart(token);
    if (isNull(queryString) || queryString.trim().isEmpty()) {
      return new LinkedList<>();
    }
    return parsedParameters(queryString);
  }

  private List<Parameter> parsedParameters(String queryString) {

    return Stream.of(queryString.split("&")).map(part -> part.split("="))
        .collect(
            Collectors.groupingBy(
                keyValue -> keyValue[0],
                LinkedHashMap::new,
                Collectors.mapping(keyValue -> keyValue[1], Collectors.toList())))
        .entrySet().stream()
        .map(entry -> new Parameter(entry.getKey(), entry.getValue()))
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private String queryPart(String token) {
    String query = "";
    if (token.contains("?") && token.indexOf("?") < token.length() - 1) {
      String[] parts = token.split(QUERY_REGEX);

      if (parts.length > 1) {
        if (parts[1].split(FRAGMENT_REGEX).length > 0) {
          query = parts[1].split(FRAGMENT_REGEX)[0];
        } else {
          return query;
        }
      } else {
        query = parts[0].split(FRAGMENT_REGEX)[0];
      }

      if (!query.isEmpty() && !query.contains("=")) {
        throw new IllegalArgumentException("Query string [" + query + "] is missing '=' operator.");
      }
    }
    return query;
  }

  public String getRootPath() {
    return rootPath;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServicePath)) return false;
    ServicePath that = (ServicePath) o;

    return paths.equals(that.paths)
        && fragments.equals(that.fragments)
        && queryParameters.size() == that.queryParameters.size()
        && queryParameters.containsAll(that.queryParameters);
  }

  public int hashCode() {
    return Objects.hash(paths, queryParameters, fragments);
  }

  private static class Parameter {
    private String key;
    private List<String> value;

    public Parameter(String key, List<String> value) {
      this.key = key;
      this.value = value;
    }

    private void addValues(List<String> moreValues) {
      value.addAll(moreValues);
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

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }

    private String asQueryString() {
      return value.stream().map(value -> key + "=" + value).collect(Collectors.joining("&"));
    }
  }
}

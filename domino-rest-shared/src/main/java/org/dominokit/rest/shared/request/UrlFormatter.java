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

import java.util.ArrayList;
import java.util.Map;
import org.dominokit.rest.shared.request.exception.PathParameterMissingException;

/** Formats the url by adding the query parameters and normalizing path parameters */
public class UrlFormatter {

  private final Map<String, String> pathParameters;

  public UrlFormatter(Map<String, String> pathParameters) {
    this.pathParameters = pathParameters;
  }

  protected String formatUrl(String targetUrl) {
    if (isNull(targetUrl)) {
      throw new IllegalArgumentException("URL cannot be null!.");
    }
    if (targetUrl.trim().isEmpty()) {
      return targetUrl.trim();
    }
    if (!hasExpressions(targetUrl)) {
      return targetUrl.trim();
    }

    String postfix = asTokenString(targetUrl);
    String prefix = targetUrl.replace(postfix, "");

    ServicePath tempToken = new ServicePath(postfix);

    replaceUrlParamsWithArguments(tempToken);

    return prefix + (targetUrl.startsWith("/") ? "/" : "") + tempToken.value();
  }

  private boolean hasExpressions(String url) {
    return (url.contains("{") && url.contains("}")) || url.contains(":");
  }

  private void replaceUrlParamsWithArguments(ServicePath tempToken) {
    replacePaths(tempToken);
  }

  private void replacePaths(ServicePath tempToken) {
    new ArrayList<>(tempToken.paths())
        .stream()
            .filter(this::isExpressionToken)
            .forEach(path -> tempToken.replacePath(path, getPathValue(path)));
  }

  private String asTokenString(String url) {
    return DominoRestContext.make().getConfig().getUrlTokenRegexMatcher().asTokenString(url);
  }

  private boolean hasPathParameter(String path) {
    String pathName = replaceExpressionMarkers(path);
    return pathParameters.containsKey(pathName);
  }

  private String getPathValue(String path) {
    if (!hasPathParameter(path)) {
      throw new PathParameterMissingException(path);
    }
    String pathName = replaceExpressionMarkers(path);
    return pathParameters.get(pathName);
  }

  private boolean isExpressionToken(String tokenPath) {
    return tokenPath.startsWith(":") || (tokenPath.startsWith("{") && tokenPath.endsWith("}"));
  }

  private String replaceExpressionMarkers(String replace) {
    return replace.replace(":", "").replace("{", "").replace("}", "");
  }
}

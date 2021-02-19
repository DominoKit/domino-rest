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
package org.dominokit.domino.rest.shared;

import static java.util.Objects.isNull;

import java.util.Arrays;

/**
 * A base implementation for the {@link RestfulRequest}
 *
 * @see RestfulRequest
 */
public abstract class BaseRestfulRequest implements RestfulRequest {

  private final String uri;
  private final String method;
  protected SuccessHandler successHandler;
  protected ErrorHandler errorHandler;
  private int timeout;

  public BaseRestfulRequest(String uri, String method) {
    if (isNull(uri) || uri.trim().isEmpty())
      throw new IllegalArgumentException("Invalid URI [" + uri + "]");
    if (isNull(method) || method.trim().isEmpty())
      throw new IllegalArgumentException("Invalid http method [" + method + "]");

    this.uri = uri;
    this.method = method;
  }

  /** {@inheritDoc} */
  @Override
  public BaseRestfulRequest addQueryString(String queryString) {
    String[] params = queryString.split("&");
    Arrays.stream(params).map(param -> param.split("=")).forEach(this::addQueryPair);
    return this;
  }

  private void addQueryPair(String[] paramNameValuePair) {
    addQueryParam(paramNameValuePair[0], paramNameValuePair[1]);
  }

  /** {@inheritDoc} */
  @Override
  public String getUri() {
    String queryParams = paramsAsString();
    return getPath() + (queryParams.isEmpty() ? queryParams : "?" + queryParams);
  }

  /** {@inheritDoc} */
  @Override
  public BaseRestfulRequest addQueryParams(String key, Iterable<String> values) {
    for (String value : values) addQueryParam(key, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public String getQuery() {
    return paramsAsString();
  }

  /** {@inheritDoc} */
  @Override
  public String getPath() {
    String path = uri;
    if (path.contains("?")) path = path.substring(0, path.indexOf("?"));

    return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
  }

  /** {@inheritDoc} */
  @Override
  public String getMethod() {
    return method;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest timeout(int timeout) {
    this.timeout = Math.max(timeout, 0);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public int getTimeout() {
    return timeout;
  }

  /** {@inheritDoc} */
  @Override
  public BaseRestfulRequest onSuccess(SuccessHandler successHandler) {
    this.successHandler = successHandler;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public BaseRestfulRequest onError(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  protected abstract String paramsAsString();
}

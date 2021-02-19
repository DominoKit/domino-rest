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
package org.dominokit.domino.rest.shared.request;

import java.util.Map;

/**
 * An interface that allows the caller to set headers, headers parameters, query parameters and path
 * parameters to the request
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public interface HasHeadersAndParameters<R, S> {
  /**
   * Sets header name and value, adding or overriding existing one
   *
   * @param name the name of the header
   * @param value the value of the header
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setHeader(String name, String value);

  /**
   * Sets a list of headers names and values
   *
   * @param headers the headers
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setHeaders(Map<String, String> headers);

  /**
   * Sets query parameter name and value
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setQueryParameter(String name, String value);

  /**
   * Sets a list of query parameters names and values
   *
   * @param queryParameters the parameters
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setQueryParameters(Map<String, String> queryParameters);

  /** use {@link #setQueryParameter(String, String)} */
  @Deprecated
  HasHeadersAndParameters<R, S> setParameter(String name, String value);

  /** use {@link #setQueryParameters(Map)} */
  @Deprecated
  HasHeadersAndParameters<R, S> setParameters(Map<String, String> queryParameters);

  /**
   * Sets a list of path parameters names and values
   *
   * @param pathParameters the parameters
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setPathParameters(Map<String, String> pathParameters);

  /**
   * Sets a path parameter name and value
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setPathParameter(String name, String value);

  /**
   * Sets a list of header parameters names and values
   *
   * @param headerParameters the parameters
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setHeaderParameters(Map<String, String> headerParameters);

  /**
   * Sets a header parameter name and value
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasHeadersAndParameters<R, S> setHeaderParameter(String name, String value);
}

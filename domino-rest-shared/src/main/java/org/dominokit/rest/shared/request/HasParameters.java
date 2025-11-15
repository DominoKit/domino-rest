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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An interface that allows the caller to set headers, header parameters, query parameters, matrix
 * parameters, and path parameters on a request.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public interface HasParameters<R, S> {

  // ---------------------- Headers ----------------------

  /**
   * Sets header name and value, adding or overriding existing one.
   *
   * @param name the name of the header
   * @param value the value of the header
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setHeader(String name, String value);

  /**
   * Sets a list of headers names and values.
   *
   * @param headers the headers
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setHeaders(Map<String, String> headers);

  // ---------------------- Query parameters ----------------------

  /**
   * Sets query parameter name and value, overriding existing values for that name.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setQueryParameter(String name, String value);

  /**
   * Sets a list of query parameters names and values, overriding existing values.
   *
   * @param queryParameters the parameters
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setQueryParameters(Map<String, List<String>> queryParameters);

  /**
   * Adds a new value to an existing query parameter (or creates it if missing).
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> addQueryParameter(String name, String value);

  /**
   * Adds new values to existing query parameters (or creates them if missing).
   *
   * @param queryParameters the parameters
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> addQueryParameters(Map<String, List<String>> queryParameters);

  // ---------------------- Matrix parameters (NEW) ----------------------

  /**
   * Sets matrix parameter name and value for the <em>current path</em> (or for the next path
   * segment to be rendered, depending on the request implementation), overriding existing values
   * for that name.
   *
   * <p>Matrix parameters are rendered on path segments using <code>;</code>, e.g. <code>
   * /users;role=admin;enabled=true</code>.
   *
   * @param name the name of the matrix parameter
   * @param value the value of the matrix parameter
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  HasParameters<R, S> setMatrixParameter(String name, String value);

  /**
   * Sets matrix parameter name and values, overriding existing values for that name.
   *
   * @param name the name of the matrix parameter
   * @param values the values of the matrix parameter
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  HasParameters<R, S> setMatrixParameter(String name, List<String> values);

  /**
   * Sets multiple matrix parameters at once, overriding existing values.
   *
   * @param matrixParameters map of matrix parameter names to their values
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  HasParameters<R, S> setMatrixParameters(Map<String, List<String>> matrixParameters);

  /**
   * Adds a matrix parameter value (or creates the parameter if missing).
   *
   * @param name the name of the matrix parameter
   * @param value the value to add
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  default HasParameters<R, S> addMatrixParameter(String name, String value) {
    return addMatrixParameter(name, Collections.singletonList(value));
  }

  /**
   * Adds matrix parameter values (or creates the parameter if missing).
   *
   * @param name the name of the matrix parameter
   * @param values the values to add
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  HasParameters<R, S> addMatrixParameter(String name, List<String> values);

  /**
   * Adds multiple matrix parameters (merging with existing ones when present).
   *
   * @param matrixParameters map of matrix parameter names to values to add
   * @return same instance to support builder pattern
   * @since 1.** (matrix params support)
   */
  HasParameters<R, S> addMatrixParameters(Map<String, List<String>> matrixParameters);

  // ---------------------- Path parameters ----------------------

  /**
   * Sets a list of path parameters names and values.
   *
   * @param pathParameters the parameters
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setPathParameters(Map<String, String> pathParameters);

  /**
   * Sets a path parameter name and value.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setPathParameter(String name, String value);

  // ---------------------- Header parameters ----------------------

  /**
   * Sets a list of header parameters names and values.
   *
   * @param headerParameters the parameters
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setHeaderParameters(Map<String, String> headerParameters);

  /**
   * Sets a header parameter name and value.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return same instance to support builder pattern
   */
  HasParameters<R, S> setHeaderParameter(String name, String value);

  HasParameters<R, S> setFragmentParameter(String name, String value);

  HasParameters<R, S> setFragmentParameters(Map<String, String> params);
}

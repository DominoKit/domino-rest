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
package org.dominokit.rest.shared.request.builder;

import jakarta.ws.rs.HttpMethod;

/**
 * Interface for specifying the HTTP method of a request.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public interface HasMethod<R, S> {

  /**
   * Specifies the HTTP method.
   *
   * @param method the HTTP method
   * @return the next builder step
   */
  HasPath<R, S> withMethod(String method);

  /**
   * Specifies the GET HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> get() {
    return withMethod(HttpMethod.GET);
  }

  /**
   * Specifies the POST HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> post() {
    return withMethod(HttpMethod.POST);
  }

  /**
   * Specifies the PUT HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> put() {
    return withMethod(HttpMethod.PUT);
  }

  /**
   * Specifies the DELETE HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> delete() {
    return withMethod(HttpMethod.DELETE);
  }

  /**
   * Specifies the PATCH HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> patch() {
    return withMethod(HttpMethod.PATCH);
  }

  /**
   * Specifies the HEAD HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> head() {
    return withMethod(HttpMethod.HEAD);
  }

  /**
   * Specifies the OPTIONS HTTP method.
   *
   * @return the next builder step
   */
  default HasPath<R, S> options() {
    return withMethod(HttpMethod.OPTIONS);
  }
}

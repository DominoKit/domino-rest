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
package org.dominokit.rest;

import org.dominokit.rest.shared.RestfulRequest;

/** A factory to create {@link RestfulRequest} */
public interface RestfulRequestFactory {

  /**
   * Creates request with uri and a method
   *
   * @param uri the request uri
   * @param method the request method
   * @return a new request
   */
  RestfulRequest request(String uri, String method);

  /**
   * Creates a POST request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest post(String uri);

  /**
   * Creates a GET request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest get(String uri);

  /**
   * Creates a PUT request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest put(String uri);

  /**
   * Creates a DELETE request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest delete(String uri);

  /**
   * Creates a HEAD request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest head(String uri);

  /**
   * Creates a OPTIONS request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest options(String uri);

  /**
   * Creates a PATCH request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  RestfulRequest patch(String uri);
}

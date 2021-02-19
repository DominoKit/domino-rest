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
package org.dominokit.domino.rest;

import org.dominokit.domino.rest.server.JavaRestfulRequest;
import org.dominokit.domino.rest.shared.RestfulRequest;

/**
 * Java platform implementation for {@link RestfulRequestFactory}
 *
 * @see RestfulRequestFactory
 */
class JavaRestfulRequestFactory implements RestfulRequestFactory {

  static {
    RestfullRequestContext.setFactory(new JavaRestfulRequestFactory());
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest request(String uri, String method) {
    return new JavaRestfulRequest(uri, method);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest get(String uri) {
    return request(uri, RestfulRequest.GET);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest post(String uri) {
    return request(uri, RestfulRequest.POST);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest delete(String uri) {
    return request(uri, RestfulRequest.DELETE);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest head(String uri) {
    return request(uri, RestfulRequest.HEAD);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest put(String uri) {
    return request(uri, RestfulRequest.PUT);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest options(String uri) {
    return request(uri, RestfulRequest.OPTIONS);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest patch(String uri) {
    return request(uri, RestfulRequest.PATCH);
  }
}

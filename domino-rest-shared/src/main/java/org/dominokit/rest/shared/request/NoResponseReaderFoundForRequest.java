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

/**
 * An exception is thrown when the request does not have a reader associated with it
 *
 * @see ServerRequest
 */
public class NoResponseReaderFoundForRequest extends RuntimeException {
  /**
   * Creates a new instance.
   *
   * @param request the {@link ServerRequest}
   * @param <R> the request type
   * @param <S> the response type
   */
  public <R, S> NoResponseReaderFoundForRequest(ServerRequest<R, S> request) {
    super(request.getMeta().toString());
  }
}

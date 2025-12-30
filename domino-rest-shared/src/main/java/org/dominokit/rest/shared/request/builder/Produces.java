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

/**
 * Interface for specifying the media type that a request produces.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public interface Produces<R, S> {
  /**
   * Specifies the media type that the request produces (e.g., "application/json").
   *
   * @param produces the media type
   * @return the configured RestRequestBuilder
   */
  RestRequestBuilder<R, S> produces(String produces);
}

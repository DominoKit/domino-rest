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

import org.dominokit.rest.shared.Response;

/** Reads the response body as a {@link String} */
public class GenericResponseReader implements ResponseReader<GenericResponse> {

  private final RequestMeta request;

  /**
   * Creates a new instance.
   *
   * @param request the {@link RequestMeta}
   */
  public GenericResponseReader(RequestMeta request) {
    this.request = request;
  }

  @Override
  public GenericResponse read(Response response) {
    return new GenericResponse(response, request);
  }
}

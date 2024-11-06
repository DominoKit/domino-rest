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

import org.dominokit.rest.shared.request.RequestMeta;
import org.dominokit.rest.shared.request.ServerRequest;

class GenericRequest<R, S> extends ServerRequest<R, S> {

  public GenericRequest(RequestMeta requestMeta, R requestBean) {
    super(requestMeta, requestBean);
  }
}

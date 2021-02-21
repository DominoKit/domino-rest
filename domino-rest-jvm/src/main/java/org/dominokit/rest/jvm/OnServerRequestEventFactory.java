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
package org.dominokit.rest.jvm;

import org.dominokit.rest.shared.Event;
import org.dominokit.rest.shared.request.FailedResponseBean;
import org.dominokit.rest.shared.request.ServerRequest;
import org.dominokit.rest.shared.request.ServerRequestEventFactory;

/** Java implementation for creating {@link Event} */
public class OnServerRequestEventFactory implements ServerRequestEventFactory {
  /** {@inheritDoc} */
  @Override
  public <T> Event makeSuccess(ServerRequest request, T responseBean) {
    return new ServerSuccessServerEvent(request, responseBean);
  }

  /** {@inheritDoc} */
  @Override
  public Event makeFailed(ServerRequest request, FailedResponseBean failedResponseBean) {
    return new ServerFailedServerEvent(request, failedResponseBean);
  }
}

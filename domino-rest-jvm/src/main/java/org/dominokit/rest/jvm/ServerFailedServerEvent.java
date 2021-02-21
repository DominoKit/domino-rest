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
import org.dominokit.rest.shared.request.Request;
import org.dominokit.rest.shared.request.ServerRequest;

/**
 * Java failed event implementation, this event sets the state of the request to received with error
 */
public class ServerFailedServerEvent implements Event {
  private final ServerRequest request;
  private final FailedResponseBean failedResponseBean;

  public ServerFailedServerEvent(ServerRequest request, FailedResponseBean failedResponseBean) {
    this.request = request;
    this.failedResponseBean = failedResponseBean;
  }

  /** {@inheritDoc} */
  @Override
  public void fire() {
    this.process();
  }

  /** {@inheritDoc} */
  @Override
  public void process() {
    request.applyState(new Request.ServerResponseReceivedStateContext(makeFailedContext()));
  }

  private Request.ServerFailedRequestStateContext makeFailedContext() {
    return new Request.ServerFailedRequestStateContext(failedResponseBean);
  }
}

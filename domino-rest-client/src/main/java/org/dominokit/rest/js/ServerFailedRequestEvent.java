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
package org.dominokit.rest.js;

import org.dominokit.rest.shared.Event;
import org.dominokit.rest.shared.EventProcessor;
import org.dominokit.rest.shared.EventsBus;
import org.dominokit.rest.shared.request.FailedResponseBean;
import org.dominokit.rest.shared.request.Request;
import org.dominokit.rest.shared.request.ServerRequest;

/**
 * JS failed event implementation, this event sets the state of the request to received with error
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ServerFailedRequestEvent extends ServerFailedRequestGwtEvent implements Event {

  /** The {@link ServerRequest} associated with this event. */
  protected final ServerRequest request;

  /** The failed response bean. */
  private final FailedResponseBean failedResponseBean;

  /**
   * Creates a new instance.
   *
   * @param request the {@link ServerRequest}
   * @param failedResponseBean the failed response bean
   */
  ServerFailedRequestEvent(ServerRequest request, FailedResponseBean failedResponseBean) {
    this.request = request;
    this.failedResponseBean = failedResponseBean;
  }

  /** {@inheritDoc} */
  @Override
  public void fire() {
    DominoSimpleEventsBus.INSTANCE.publishEvent(new GWTRequestEvent(this));
  }

  /** {@inheritDoc} */
  @Override
  public void process() {
    request.applyState(new Request.ServerResponseReceivedStateContext(makeFailedContext()));
  }

  private Request.ServerFailedRequestStateContext makeFailedContext() {
    return new Request.ServerFailedRequestStateContext(failedResponseBean);
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(EventProcessor eventProcessor) {
    eventProcessor.process(this);
  }

  private class GWTRequestEvent implements EventsBus.RequestEvent<ServerFailedRequestGwtEvent> {

    private final ServerFailedRequestGwtEvent event;

    public GWTRequestEvent(ServerFailedRequestGwtEvent event) {
      this.event = event;
    }

    @Override
    public ServerFailedRequestGwtEvent asEvent() {
      return event;
    }
  }
}

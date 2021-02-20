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
package org.dominokit.domino.rest.shared.request;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the abstract implementation for {@link RequestAsyncSender} that uses the {@link
 * AsyncRunner} defined in the {@link RestConfig}.
 *
 * <p>If the async operation runs successfully, then the request will be sent, otherwise and error
 * will logged saying that the request could be sent.
 *
 * @see RequestAsyncSender
 * @see DominoRestContext
 * @see RestConfig
 * @see AsyncRunner
 * @see ServerRequestEventFactory
 */
public abstract class AbstractRequestAsyncSender implements RequestAsyncSender {

  private static final Logger LOGGER = Logger.getLogger(RequestAsyncSender.class.getName());
  private final ServerRequestEventFactory requestEventFactory;

  public AbstractRequestAsyncSender(ServerRequestEventFactory requestEventFactory) {
    this.requestEventFactory = requestEventFactory;
  }

  /** {@inheritDoc} */
  @SuppressWarnings("rawtypes")
  @Override
  public final void send(final ServerRequest request) {
    DominoRestContext.make().getConfig().asyncRunner().runAsync(new RequestAsyncTask(request));
  }

  private class RequestAsyncTask implements AsyncRunner.AsyncTask {
    private final ServerRequest request;

    private RequestAsyncTask(ServerRequest request) {
      this.request = request;
    }

    @Override
    public void onSuccess() {
      sendRequest(request, requestEventFactory);
    }

    @Override
    public void onFailed(Throwable error) {
      LOGGER.log(Level.SEVERE, "Could not RunAsync request [" + request + "]", error);
    }
  }

  protected abstract void sendRequest(
      ServerRequest request, ServerRequestEventFactory requestEventFactory);
}

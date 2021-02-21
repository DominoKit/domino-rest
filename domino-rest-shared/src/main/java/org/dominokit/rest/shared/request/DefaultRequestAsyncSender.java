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
 * The default {@link RequestAsyncSender} that sends the request using the {@link RequestRestSender}
 * and sends events to the request event factory based on the result
 *
 * @see RequestAsyncSender
 * @see RequestRestSender
 * @see ServerRequestEventFactory
 */
public class DefaultRequestAsyncSender extends AbstractRequestAsyncSender {

  private final RequestRestSender requestSender;

  public DefaultRequestAsyncSender(
      ServerRequestEventFactory requestEventFactory, RequestRestSender requestSender) {
    super(requestEventFactory);
    this.requestSender = requestSender;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected void sendRequest(ServerRequest request, ServerRequestEventFactory requestEventFactory) {
    requestSender.send(
        request,
        new ServerRequestCallBack() {

          @Override
          public <T> void onSuccess(T response) {
            requestEventFactory.makeSuccess(request, response).fire();
          }

          @Override
          public void onFailure(FailedResponseBean failedResponse) {
            requestEventFactory.makeFailed(request, failedResponse).fire();
          }
        });
  }
}

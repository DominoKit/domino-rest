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
 * The default server router which routes the request in an async mode using {@link
 * RequestAsyncSender}
 *
 * @see RequestAsyncSender
 * @see ServerRequest
 */
public class ServerRouter implements RequestRouter<ServerRequest> {

  private final RequestAsyncSender requestAsyncRunner;

  public ServerRouter(RequestAsyncSender requestAsyncRunner) {
    this.requestAsyncRunner = requestAsyncRunner;
  }

  /** {@inheritDoc} */
  @Override
  public void routeRequest(final ServerRequest request) {
    requestAsyncRunner.send(request);
  }
}

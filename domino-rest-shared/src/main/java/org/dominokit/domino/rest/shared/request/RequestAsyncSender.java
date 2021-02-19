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

/**
 * Sends the request asynchronously.
 *
 * <p>The {@link ServerRouter} uses this sender to send the request in an async mode and it is the
 * sender responsibility to handle the result in an async mode.
 *
 * @see ServerRequest
 * @see ServerRouter
 */
@FunctionalInterface
public interface RequestAsyncSender {

  /**
   * Send the request in an async mode.
   *
   * @param request the request to send
   */
  void send(ServerRequest request);
}

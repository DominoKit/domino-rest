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
 * Sends the request using REST.
 *
 * <p>This will send the request using REST and assign a callback to handle if it was successful or
 * failure
 *
 * @param <T> the request type
 * @param <S> the response type
 * @see ServerRequest
 * @see ServerRequestCallBack
 */
public interface RequestRestSender<T, S> {
  /**
   * Sends the request and call the callback based on the result
   *
   * @param request the request to send
   * @param callBack the callback that behaves based on the response status
   */
  void send(ServerRequest<T, S> request, ServerRequestCallBack callBack);
}

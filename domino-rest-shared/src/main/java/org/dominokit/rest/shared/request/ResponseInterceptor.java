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

/**
 * Intercepts the response.
 *
 * @see ServerRequest
 * @see Response
 * @see FailedResponseBean
 */
public interface ResponseInterceptor {
  /**
   * @param serverRequest
   * @param response
   * @deprecated use {@link #onBeforeSuccessCallback(ServerRequest, Response)} instead
   */
  @Deprecated
  default void interceptOnSuccess(ServerRequest serverRequest, Response response) {}

  default void onBeforeSuccessCallback(ServerRequest serverRequest, Response response) {
    interceptOnSuccess(serverRequest, response);
  }

  /**
   * Intercepts the failed response
   *
   * @param serverRequest
   * @param failedResponse
   * @deprecated use {@link #onBeforeFailedCallback(ServerRequest, FailedResponseBean)} instead
   */
  @Deprecated
  default void interceptOnFailed(ServerRequest serverRequest, FailedResponseBean failedResponse) {}

  /**
   * Intercepts the failed response
   *
   * @param serverRequest
   * @param failedResponse
   */
  default void onBeforeFailedCallback(
      ServerRequest serverRequest, FailedResponseBean failedResponse) {
    interceptOnFailed(serverRequest, failedResponse);
  }

  default void onBeforeCompleteCallback(ServerRequest serverRequest) {}

  default void onAfterCompleteCallback(ServerRequest serverRequest) {}
}

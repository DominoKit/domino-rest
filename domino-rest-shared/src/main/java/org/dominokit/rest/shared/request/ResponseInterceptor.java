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
 * Intercepts responses at different stages of a {@link ServerRequest} lifecycle.
 *
 * <p>Each method is a no-op by default so implementors only override the hooks they need.
 *
 * @see ServerRequest
 * @see Response
 * @see FailedResponseBean
 */
public interface ResponseInterceptor {
  /**
   * Called before a successful callback is invoked.
   *
   * @param serverRequest the request that produced the response
   * @param response the successful response
   * @deprecated use {@link #onBeforeSuccessCallback(ServerRequest, Response)} instead
   */
  @Deprecated
  default void interceptOnSuccess(ServerRequest serverRequest, Response response) {}

  /**
   * Hook invoked before a successful callback.
   *
   * @param serverRequest the request that produced the response
   * @param response the successful response
   */
  default void onBeforeSuccessCallback(ServerRequest serverRequest, Response response) {
    interceptOnSuccess(serverRequest, response);
  }

  /**
   * Called before a failed callback is invoked.
   *
   * @param serverRequest the request that failed
   * @param failedResponse the failure details
   * @deprecated use {@link #onBeforeFailedCallback(ServerRequest, FailedResponseBean)} instead
   */
  @Deprecated
  default void interceptOnFailed(ServerRequest serverRequest, FailedResponseBean failedResponse) {}

  /**
   * Hook invoked before a failed callback.
   *
   * @param serverRequest the request that failed
   * @param failedResponse the failure details
   */
  default void onBeforeFailedCallback(
      ServerRequest serverRequest, FailedResponseBean failedResponse) {
    interceptOnFailed(serverRequest, failedResponse);
  }

  /**
   * Hook executed before completion callbacks are invoked regardless of success or failure.
   *
   * @param serverRequest the request that is completing
   */
  default void onBeforeCompleteCallback(ServerRequest serverRequest) {}

  /**
   * Hook executed after completion callbacks finish regardless of success or failure.
   *
   * @param serverRequest the request that completed
   */
  default void onAfterCompleteCallback(ServerRequest serverRequest) {}
}

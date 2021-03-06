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
 * A callback for the request that will be called either on success request or on failure
 *
 * @see FailedResponseBean
 */
public interface ServerRequestCallBack {
  /**
   * This will be called when the request failed
   *
   * @param failedResponse the failed response containing all the details
   */
  void onFailure(FailedResponseBean failedResponse);

  /**
   * This will be called when the request succeed
   *
   * @param response the response
   * @param <T> the response type
   */
  <T> void onSuccess(T response);
}

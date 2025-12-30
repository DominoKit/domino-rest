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

import static org.dominokit.domino.api.shared.extension.ContextAggregator.ContextWait;

import org.dominokit.domino.api.shared.extension.ContextAggregator;

/**
 * Intercepts the request and the response to modify them either before sending the request or after
 * received the response from the server.
 *
 * <p>This can be assigned globally using {@link
 * RestConfig#addRequestInterceptor(RequestInterceptor)} and it will be called for each request and
 * response.
 *
 * <p>The interceptor will be blocked till the context wait get completed.
 *
 * <p>For example:
 *
 * <pre>
 * public class TokenInterceptor implements RequestInterceptor {
 *     &#64;Override
 *     public void interceptRequest(ServerRequest request, ContextAggregator.ContextWait&#60;ServerRequest&#62; contextWait) {
 *         request.setHeader("Authorization", "some token goes here");
 *         contextWait.complete(request);
 *     }
 * }
 * </pre>
 *
 * @see ContextWait
 * @see ServerRequest
 */
public interface RequestInterceptor {
  /**
   * Intercepts the request.
   *
   * @param request the {@link ServerRequest} to intercept
   * @param contextWait the {@link ContextWait} to signal completion
   */
  void interceptRequest(
      ServerRequest request, ContextAggregator.ContextWait<ServerRequest> contextWait);
}

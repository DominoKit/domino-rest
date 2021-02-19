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
 * An interface represents the state of the request
 *
 * @param <C> the context of the state
 * @see RequestStateContext
 */
@FunctionalInterface
public interface RequestState<C extends RequestStateContext> {
  /**
   * Change the request based on its state, each state will do the appropriate changes on the
   * request.
   *
   * @param request the request
   */
  void execute(C request);
}

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
package org.dominokit.domino.rest.shared;

/**
 * A middleware tunnel that consume and produce events
 *
 * @param <T> the request type
 */
@FunctionalInterface
public interface EventsBus<T> {

  @FunctionalInterface
  interface RequestEvent<T> {
    T asEvent();
  }

  /**
   * put the event on the bus to be delivered for all listeners
   *
   * @param event the event to publish
   */
  void publishEvent(RequestEvent<T> event);
}

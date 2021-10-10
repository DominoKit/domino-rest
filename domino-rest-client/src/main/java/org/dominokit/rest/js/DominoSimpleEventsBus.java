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
package org.dominokit.rest.js;

import org.dominokit.rest.shared.EventProcessor;
import org.dominokit.rest.shared.EventsBus;
import org.gwtproject.event.shared.Event;
import org.gwtproject.event.shared.EventBus;
import org.gwtproject.event.shared.SimpleEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Event bus implementation that uses GWT {@link EventBus} */
public class DominoSimpleEventsBus implements EventsBus<Event<EventProcessor>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DominoSimpleEventsBus.class);

  public static final EventsBus INSTANCE = new DominoSimpleEventsBus(new EventProcessor());

  private final EventBus simpleGwtEventsBus;
  private final EventProcessor eventProcessor;

  public DominoSimpleEventsBus(EventProcessor eventProcessor) {
    this.simpleGwtEventsBus = new SimpleEventBus();
    this.eventProcessor = eventProcessor;
    addEvent(ServerSuccessRequestGwtEvent.SERVER_SUCCESS_REQUEST_EVENT_TYPE);
    addEvent(ServerFailedRequestGwtEvent.SERVER_FAILED_REQUEST_EVENT_TYPE);
  }

  /**
   * Adds event type to handle
   *
   * @param type the type of the event
   */
  public void addEvent(Event.Type<EventProcessor> type) {
    simpleGwtEventsBus.addHandler(type, eventProcessor);
  }

  /** {@inheritDoc} */
  @Override
  public void publishEvent(RequestEvent<Event<EventProcessor>> event) {
    //    try {
    simpleGwtEventsBus.fireEvent(event.asEvent());
    //    } catch (Exception ex) {
    //      LOGGER.error("could not publish event", ex);
    //      throw ex;
    //    }
  }
}

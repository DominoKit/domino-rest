package org.dominokit.domino.rest.gwt;

import org.dominokit.domino.rest.shared.EventProcessor;
import org.gwtproject.event.shared.Event;

public abstract class ServerSuccessRequestGwtEvent extends Event<EventProcessor> {

    static final Event.Type<EventProcessor> SERVER_SUCCESS_REQUEST_EVENT_TYPE = new Event.Type<>();

    @Override
    public Type<EventProcessor> getAssociatedType() {
        return SERVER_SUCCESS_REQUEST_EVENT_TYPE;
    }

}
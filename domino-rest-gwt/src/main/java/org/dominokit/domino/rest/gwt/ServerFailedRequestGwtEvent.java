package org.dominokit.domino.rest.gwt;

import org.dominokit.domino.rest.shared.EventProcessor;
import org.gwtproject.event.shared.Event;

public abstract class ServerFailedRequestGwtEvent extends Event<EventProcessor> {

    protected static final Event.Type<EventProcessor> SERVER_FAILED_REQUEST_EVENT_TYPE = new Event.Type<>();

    @Override
    public Type<EventProcessor> getAssociatedType() {
        return SERVER_FAILED_REQUEST_EVENT_TYPE;
    }

}
package org.dominokit.domino.rest.gwt;

import org.dominokit.domino.rest.shared.EventProcessor;
import org.dominokit.domino.rest.shared.EventsBus;
import org.gwtproject.event.shared.Event;
import org.gwtproject.event.shared.EventBus;
import org.gwtproject.event.shared.SimpleEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DominoSimpleEventsBus implements EventsBus<Event<GwtEventProcessor>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DominoSimpleEventsBus.class);

    public static final EventsBus INSTANCE = new DominoSimpleEventsBus(new RequestEventProcessor());

    private final EventBus simpleGwtEventsBus;
    private final GwtEventProcessor eventProcessor;

    public DominoSimpleEventsBus(GwtEventProcessor eventProcessor) {
        this.simpleGwtEventsBus = new SimpleEventBus();
        this.eventProcessor = eventProcessor;
        addEvent(ServerSuccessRequestGwtEvent.SERVER_SUCCESS_REQUEST_EVENT_TYPE);
        addEvent(ServerFailedRequestGwtEvent.SERVER_FAILED_REQUEST_EVENT_TYPE);
    }

    public void addEvent(Event.Type<EventProcessor> type){
        simpleGwtEventsBus.addHandler(type, eventProcessor);
    }

    @Override
    public void publishEvent(RequestEvent<Event<GwtEventProcessor>> event) {
        try {
            simpleGwtEventsBus.fireEvent(event.asEvent());
        } catch (Exception ex) {
            LOGGER.error("could not publish event", ex);
        }
    }
}

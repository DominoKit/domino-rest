package org.dominokit.domino.rest.gwt;

import org.dominokit.domino.rest.shared.Event;

public class RequestEventProcessor implements GwtEventProcessor {

    @Override
    public void process(Event event) {
        event.process();
    }
}

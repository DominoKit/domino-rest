package org.dominokit.domino.rest.gwt;


import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.EventProcessor;
import org.dominokit.domino.rest.shared.EventsBus;
import org.dominokit.domino.rest.shared.request.Request;
import org.dominokit.domino.rest.shared.request.ServerRequest;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServerSuccessRequestEvent<T> extends ServerSuccessRequestGwtEvent implements Event {

    protected final ServerRequest request;
    private final T responseBean;

    public ServerSuccessRequestEvent(ServerRequest request, T responseBean) {
        this.request = request;
        this.responseBean = responseBean;
    }

    @Override
    public void fire() {
        DominoSimpleEventsBus.INSTANCE.publishEvent(new GWTRequestEvent(this));
    }

    @Override
    public void process() {
        request.applyState(new Request.ServerResponseReceivedStateContext(makeSuccessContext()));
    }

    private Request.ServerSuccessRequestStateContext makeSuccessContext() {
        return new Request.ServerSuccessRequestStateContext(responseBean);
    }

    @Override
    protected void dispatch(EventProcessor eventProcessor) {
        eventProcessor.process(this);
    }

    private class GWTRequestEvent implements EventsBus.RequestEvent<ServerSuccessRequestGwtEvent> {

        private final ServerSuccessRequestGwtEvent event;

        public GWTRequestEvent(ServerSuccessRequestGwtEvent event) {
            this.event = event;
        }

        @Override
        public ServerSuccessRequestGwtEvent asEvent() {
            return event;
        }
    }
}

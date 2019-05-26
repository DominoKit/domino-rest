package org.dominokit.domino.rest.gwt;


import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.EventProcessor;
import org.dominokit.domino.rest.shared.EventsBus;
import org.dominokit.domino.rest.shared.request.FailedResponseBean;
import org.dominokit.domino.rest.shared.request.Request;
import org.dominokit.domino.rest.shared.request.ServerRequest;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServerFailedRequestEvent extends ServerFailedRequestGwtEvent implements Event {

    protected final ServerRequest request;
    private final FailedResponseBean failedResponseBean;

    ServerFailedRequestEvent(ServerRequest request, FailedResponseBean failedResponseBean) {
        this.request = request;
        this.failedResponseBean = failedResponseBean;
    }

    @Override
    public void fire() {
        DominoSimpleEventsBus.INSTANCE.publishEvent(new GWTRequestEvent(this));
    }

    @Override
    public void process() {
        request.applyState(new Request.ServerResponseReceivedStateContext(makeFailedContext()));
    }

    private Request.ServerFailedRequestStateContext makeFailedContext() {
        return new Request.ServerFailedRequestStateContext(failedResponseBean);
    }

    @Override
    protected void dispatch(EventProcessor eventProcessor) {
        eventProcessor.process(this);
    }

    private class GWTRequestEvent implements EventsBus.RequestEvent<ServerFailedRequestGwtEvent> {

        private final ServerFailedRequestGwtEvent event;

        public GWTRequestEvent(ServerFailedRequestGwtEvent event) {
            this.event = event;
        }

        @Override
        public ServerFailedRequestGwtEvent asEvent() {
            return event;
        }
    }
}

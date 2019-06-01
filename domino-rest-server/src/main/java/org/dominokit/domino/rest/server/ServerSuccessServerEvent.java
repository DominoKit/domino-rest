package org.dominokit.domino.rest.server;

import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.request.Request;
import org.dominokit.domino.rest.shared.request.ServerRequest;

public class ServerSuccessServerEvent<T> implements Event {
    private final ServerRequest request;
    private final T responseBean;

    public ServerSuccessServerEvent(ServerRequest request, T responseBean) {
        this.request = request;
        this.responseBean = responseBean;
    }

    @Override
    public void fire() {
        this.process();
    }

    @Override
    public void process() {
        request.applyState(new Request.ServerResponseReceivedStateContext(makeSuccessContext()));
    }

    private Request.ServerSuccessRequestStateContext makeSuccessContext() {
        return new Request.ServerSuccessRequestStateContext(responseBean);
    }
}

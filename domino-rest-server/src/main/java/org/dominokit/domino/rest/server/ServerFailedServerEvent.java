package org.dominokit.domino.rest.server;


import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.request.FailedResponseBean;
import org.dominokit.domino.rest.shared.request.Request;
import org.dominokit.domino.rest.shared.request.ServerRequest;

public class ServerFailedServerEvent implements Event {
    private final ServerRequest request;
    private final FailedResponseBean failedResponseBean;

    public ServerFailedServerEvent(ServerRequest request, FailedResponseBean failedResponseBean) {
        this.request = request;
        this.failedResponseBean = failedResponseBean;
    }

    @Override
    public void fire() {
        this.process();
    }

    @Override
    public void process() {
        request.applyState(new Request.ServerResponseReceivedStateContext(makeFailedContext()));
    }

    private Request.ServerFailedRequestStateContext makeFailedContext() {
        return new Request.ServerFailedRequestStateContext(failedResponseBean);
    }
}

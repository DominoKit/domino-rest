package org.dominokit.domino.rest.server;


import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.request.FailedResponseBean;
import org.dominokit.domino.rest.shared.request.ServerRequest;
import org.dominokit.domino.rest.shared.request.ServerRequestEventFactory;

public class OnServerRequestEventFactory implements ServerRequestEventFactory {
    @Override
    public <T> Event makeSuccess(ServerRequest request, T responseBean) {
        return new ServerSuccessServerEvent(request, responseBean);
    }

    @Override
    public Event makeFailed(ServerRequest request, FailedResponseBean failedResponseBean) {
        return new ServerFailedServerEvent(request, failedResponseBean);
    }
}

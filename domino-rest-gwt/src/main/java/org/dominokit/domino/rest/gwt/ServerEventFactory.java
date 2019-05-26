package org.dominokit.domino.rest.gwt;


import org.dominokit.domino.rest.shared.Event;
import org.dominokit.domino.rest.shared.request.FailedResponseBean;
import org.dominokit.domino.rest.shared.request.ServerRequest;
import org.dominokit.domino.rest.shared.request.ServerRequestEventFactory;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServerEventFactory implements ServerRequestEventFactory {
    @Override
    public <T> Event makeSuccess(ServerRequest request, T responseBean) {
        return new ServerSuccessRequestEvent(request, responseBean);
    }

    @Override
    public Event makeFailed(ServerRequest request, FailedResponseBean failedResponseBean) {
        return new ServerFailedRequestEvent(request, failedResponseBean);
    }
}

package org.dominokit.domino.rest.shared.request;


import org.dominokit.domino.rest.shared.Event;

public interface ServerRequestEventFactory {
    <T> Event makeSuccess(ServerRequest request, T responseBean);
    Event makeFailed(ServerRequest request, FailedResponseBean failedResponse);
}

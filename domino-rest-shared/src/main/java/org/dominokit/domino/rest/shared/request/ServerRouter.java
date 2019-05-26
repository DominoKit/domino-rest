package org.dominokit.domino.rest.shared.request;

public class ServerRouter implements RequestRouter<ServerRequest> {

    private final RequestAsyncSender requestAsyncRunner;

    public ServerRouter(RequestAsyncSender requestAsyncRunner) {
        this.requestAsyncRunner = requestAsyncRunner;
    }

    @Override
    public void routeRequest(final ServerRequest request) {
        requestAsyncRunner.send(request);
    }
}

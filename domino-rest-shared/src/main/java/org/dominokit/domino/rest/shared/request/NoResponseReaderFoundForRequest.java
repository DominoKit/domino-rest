package org.dominokit.domino.rest.shared.request;

public class NoResponseReaderFoundForRequest extends RuntimeException {
    public <R, S> NoResponseReaderFoundForRequest(ServerRequest<R, S> request) {
        super(request.getMeta().toString());
    }
}

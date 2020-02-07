package org.dominokit.domino.rest.shared.request;

public class NoRequestWriterFoundForRequest extends RuntimeException {
    public <R, S> NoRequestWriterFoundForRequest(ServerRequest<R, S> request) {
        super(request.getMeta().toString());
    }
}

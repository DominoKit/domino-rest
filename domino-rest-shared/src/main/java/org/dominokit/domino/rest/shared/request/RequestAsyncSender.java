package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface RequestAsyncSender {
    void send(ServerRequest request);
}

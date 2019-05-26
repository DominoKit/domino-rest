package org.dominokit.domino.rest.shared.request;


@FunctionalInterface
public interface RequestRouter<R extends Request> {
    void routeRequest(R request);
}

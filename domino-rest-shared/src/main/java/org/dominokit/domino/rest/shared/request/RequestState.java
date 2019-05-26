package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface RequestState<C extends RequestStateContext> {
    void execute(C request);
}

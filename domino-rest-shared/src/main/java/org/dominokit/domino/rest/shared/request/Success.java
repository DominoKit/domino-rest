package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface Success<S > {
    void onSuccess(S response);
}

package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface Fail {
    void onFail(FailedResponseBean failedResponse);
}

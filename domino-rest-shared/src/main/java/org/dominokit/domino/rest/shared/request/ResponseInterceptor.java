package org.dominokit.domino.rest.shared.request;

public interface ResponseInterceptor {
    default void interceptOnSuccess(ServerRequest serverRequest, String body){
    }

    default void interceptOnFailed(ServerRequest serverRequest, FailedResponseBean failedResponse){
    }
}

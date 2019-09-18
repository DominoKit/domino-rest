package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.rest.shared.Response;

public interface ResponseInterceptor {
    default void interceptOnSuccess(ServerRequest serverRequest, Response response){
    }

    default void interceptOnFailed(ServerRequest serverRequest, FailedResponseBean failedResponse){
    }
}

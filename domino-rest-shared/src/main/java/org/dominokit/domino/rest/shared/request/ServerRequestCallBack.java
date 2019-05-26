package org.dominokit.domino.rest.shared.request;

public interface ServerRequestCallBack {
    void onFailure(FailedResponseBean failedResponse);
    <T> void  onSuccess(T response);
}

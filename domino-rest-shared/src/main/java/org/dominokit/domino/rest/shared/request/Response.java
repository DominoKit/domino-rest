package org.dominokit.domino.rest.shared.request;

public interface Response<S> {
    CanFailOrSend onSuccess(Success<S> success);
}

package org.dominokit.domino.rest.shared.request;

public interface HasFail {
    CanCompleteOrSend onFailed(Fail fail);
}

package org.dominokit.domino.rest.shared.request;

public interface HasFail {
    CanSend onFailed(Fail fail);
}

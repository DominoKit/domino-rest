package org.dominokit.domino.rest.shared.request;

public interface HasComplete extends CanFailOrSend{
    CanFailOrSend onComplete(CompleteHandler completeHandler);
}

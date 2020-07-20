package org.dominokit.domino.rest.shared.request;

public interface CanCompleteOrSend extends CanSend{
    CanSend onComplete(CompleteHandler completeHandler);
}

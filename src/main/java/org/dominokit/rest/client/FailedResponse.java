package org.dominokit.rest.client;

public class FailedResponse extends Throwable {
    public FailedResponse(String responseText) {
        super(responseText);
    }
}

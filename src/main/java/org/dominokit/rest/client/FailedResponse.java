package org.dominokit.rest.client;

public class FailedResponse extends Throwable {

    private int statusCode = -1;

    public FailedResponse(String responseText) {
        super(responseText);
    }

    public FailedResponse(int statusCode, String responseText) {
        super(responseText);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

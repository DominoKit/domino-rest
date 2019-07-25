package org.dominokit.domino.rest.shared.request;

import java.util.Map;

public class FailedResponse extends Throwable {

    private final int statusCode;
    private final String responseText;
    private final String bodyAsString;
    private final Map<String, String> headers;

    public FailedResponse(int statusCode, String responseText, String bodyAsString, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.responseText = responseText;
        this.bodyAsString = bodyAsString;
        this.headers = headers;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public String getBodyAsString() {
        return bodyAsString;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}

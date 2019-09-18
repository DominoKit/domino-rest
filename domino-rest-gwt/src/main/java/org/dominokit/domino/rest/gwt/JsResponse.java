package org.dominokit.domino.rest.gwt;

import elemental2.core.ArrayBuffer;
import org.dominokit.domino.rest.shared.Response;
import org.gwtproject.xhr.client.XMLHttpRequest;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsResponse implements Response {

    private final XMLHttpRequest request;

    JsResponse(XMLHttpRequest request) {
        this.request = request;
    }

    @Override
    public String getHeader(String header) {
        return request.getResponseHeader(header);
    }

    @Override
    public Map<String, String> getHeaders() {
        String allResponseHeaders = request.getAllResponseHeaders();
        String[] headers = allResponseHeaders.split("\n");
        return Stream.of(headers)
                .filter(header -> !header.isEmpty())
                .map(header -> header.split(":", 2))
                .collect(Collectors.toMap(header -> header[0], header -> header[1].trim()));
    }

    @Override
    public int getStatusCode() {
        return request.getStatus();
    }

    @Override
    public String getStatusText() {
        return request.getStatusText();
    }

    @Override
    public String getBodyAsString() {
        return request.getResponseText();
    }

    public ArrayBuffer getResponseArrayBuffer() {
        return request.getResponseArrayBuffer();
    }
}

package org.dominokit.rest.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.xhr.client.XMLHttpRequest;
import org.dominokit.rest.shared.BaseRestfulRequest;
import org.dominokit.rest.shared.RestfulRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class JsRestfulRequest extends BaseRestfulRequest {

    private XMLHttpRequest request;
    private Map<String, List<String>> params = new LinkedHashMap<>();
    private Map<String, String> headers = new LinkedHashMap<>();
    private final Timer timer = new Timer() {
        @Override
        public void run() {
            fireOnTimeout();
        }
    };

    public JsRestfulRequest(String uri, String method) {
        super(uri, method);
        request = XMLHttpRequest.create();
        parseUri(uri);
    }

    private void parseUri(String uri) {
        if (uri.contains("?")) {
            String[] uriParts = uri.split("\\?");
            addQueryString(uriParts[1]);
        }
    }

    @Override
    protected String paramsAsString() {
        return params.entrySet().stream()
                .map(this::entryAsString)
                .collect(joining("&"));
    }

    private String entryAsString(Map.Entry<String, List<String>> paramValuePair) {
        return paramValuePair.getValue().stream()
                .map(v -> paramValuePair.getKey() + "=" + v)
                .collect(joining("&"));
    }

    @Override
    public RestfulRequest addQueryParam(String key, String value) {
        if (!params.containsKey(key))
            params.put(key, new ArrayList<>());
        params.get(key).add(value);
        return this;
    }

    @Override
    public RestfulRequest setQueryParam(String key, String value) {
        params.put(key, new ArrayList<>());
        addQueryParam(key, value);
        return this;
    }

    @Override
    public RestfulRequest putHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void sendForm(Map<String, String> formData) {
        setContentType("application/x-www-form-urlencoded");
        send(formData.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(joining("&")));
    }

    @Override
    public void sendJson(String json) {
        setContentType("application/json");
        send(json);
    }

    @Override
    public void send(String data) {
        initRequest();
        request.send(data);
    }

    @Override
    public void send() {
        initRequest();
        request.send();
    }

    private void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

    private void initRequest() {
        request.open(getMethod(), getUri());
        setHeaders();
        request.setOnReadyStateChange(xhr -> {
            if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                xhr.clearOnReadyStateChange();
                timer.cancel();
                successHandler.onResponseReceived(new JsResponse(xhr));
            }
        });
        if (getTimeout() > 0) {
            timer.schedule(getTimeout());
        }
    }

    private void fireOnTimeout() {
        timer.cancel();
        request.clearOnReadyStateChange();
        request.abort();
        errorHandler.onError(new RequestTimeoutException());
    }

    private void setHeaders() {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.setRequestHeader(entry.getKey(), entry.getValue());
        }
    }
}

package org.dominokit.domino.rest.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.dominokit.domino.rest.VertxInstanceProvider;
import org.dominokit.domino.rest.shared.BaseRestfulRequest;
import org.dominokit.domino.rest.shared.GwtIncompatible;
import org.dominokit.domino.rest.shared.RestfulRequest;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

@GwtIncompatible
public class JavaRestfulRequest extends BaseRestfulRequest {

    private final HttpRequest<Buffer> request;
    private WebClient webClient;

    public JavaRestfulRequest(String uri, String method) {
        super(uri, method);
        request = getWebClient().requestAbs(HttpMethod.valueOf(method), uri);
    }

    @Override
    protected String paramsAsString() {
        return request.queryParams()
                .entries().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(joining("&"));
    }

    @Override
    public JavaRestfulRequest addQueryParam(String key, String value) {
        request.addQueryParam(key, value);
        return this;
    }

    @Override
    public JavaRestfulRequest setQueryParam(String key, String value) {
        request.setQueryParam(key, value);
        return this;
    }

    @Override
    public JavaRestfulRequest putHeader(String key, String value) {
        request.putHeader(key, value);
        return this;
    }

    @Override
    public RestfulRequest putHeaders(Map<String, String> headers) {
        if (nonNull(headers)) {
            headers.forEach(this::putHeader);
        }
        return this;
    }

    @Override
    public RestfulRequest putParameters(Map<String, String> parameters) {
        if (nonNull(parameters) && !parameters.isEmpty()) {
            parameters.forEach(this::addQueryParam);
        }
        return this;
    }

    @Override
    public Map<String, String> getHeaders() {
        return request.headers().entries().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public RestfulRequest timeout(int timeout) {
        request.timeout(timeout);
        return super.timeout(timeout);
    }

    @Override
    public void sendForm(Map<String, String> formData) {
        request.sendForm(MultiMap.caseInsensitiveMultiMap().addAll(formData), this::handleResponse);
    }

    @Override
    public void sendJson(String json) {
        putHeader("Content-Type", "application/json");
        send(json);
    }

    @Override
    public void send(String data) {
        request.sendBuffer(Buffer.buffer(data), this::handleResponse);
    }

    @Override
    public void send() {
        request.send(this::handleResponse);
    }

    @Override
    public void abort() {
        //TODO not implemented yet
    }

    private void handleResponse(AsyncResult<HttpResponse<Buffer>> event) {
        if (event.succeeded())
            successHandler.onResponseReceived(new JavaResponse(event.result()));
        else
            errorHandler.onError(event.cause());
    }

    @Override
    public RestfulRequest setResponseType(String responseType) {
        return this;
    }

    private WebClient getWebClient() {
        if (isNull(this.webClient)) {
            Iterator<VertxInstanceProvider> iterator = ServiceLoader.load(VertxInstanceProvider.class).iterator();
            Vertx instance;
            if (iterator.hasNext()) {
                instance = iterator.next().getInstance();
            } else {
                instance = Vertx.vertx();
            }
            this.webClient = WebClient.create(instance);
        }

        return webClient;
    }
}

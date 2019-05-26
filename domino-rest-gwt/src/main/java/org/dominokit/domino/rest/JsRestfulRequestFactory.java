package org.dominokit.domino.rest;

import org.dominokit.domino.rest.gwt.JsRestfulRequest;
import org.dominokit.domino.rest.shared.RestfulRequest;

public class JsRestfulRequestFactory implements RestfulRequestFactory {

    public RestfulRequest request(String uri, String method) {
        return new JsRestfulRequest(uri, method);
    }

    public RestfulRequest get(String uri) {
        return request(uri, RestfulRequest.GET);
    }

    public RestfulRequest post(String uri) {
        return request(uri, RestfulRequest.POST);
    }

    public RestfulRequest delete(String uri) {
        return request(uri, RestfulRequest.DELETE);
    }

    public RestfulRequest head(String uri) {
        return request(uri, RestfulRequest.HEAD);
    }

    public RestfulRequest put(String uri) {
        return request(uri, RestfulRequest.PUT);
    }

    public RestfulRequest options(String uri) {
        return request(uri, RestfulRequest.OPTIONS);
    }

    public RestfulRequest patch(String uri) {
        return request(uri, RestfulRequest.PATCH);
    }
}

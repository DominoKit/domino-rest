package org.dominokit.rest.shared;

import org.dominokit.rest.client.JsRestfulRequest;

class JsRestfulRequestFactory {

    RestfulRequest request(String uri, String method) {
        return new JsRestfulRequest(uri, method);
    }

    RestfulRequest get(String uri) {
        return request(uri, RestfulRequest.GET);
    }

    RestfulRequest post(String uri) {
        return request(uri, RestfulRequest.POST);
    }

    RestfulRequest delete(String uri) {
        return request(uri, RestfulRequest.DELETE);
    }

    RestfulRequest head(String uri) {
        return request(uri, RestfulRequest.HEAD);
    }

    RestfulRequest put(String uri) {
        return request(uri, RestfulRequest.PUT);
    }

    RestfulRequest options(String uri) {
        return request(uri, RestfulRequest.OPTIONS);
    }
}

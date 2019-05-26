package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.RestfulRequest;

class JavaRestfulRequestFactory implements RestfulRequestFactory {

    static {
        RestfullRequestContext.setFactory(new JavaRestfulRequestFactory());
    }

    @Override
    public RestfulRequest request(String uri, String method) {
        return new JavaRestfulRequest(uri, method);
    }

    @Override
    public RestfulRequest get(String uri) {
        return request(uri, RestfulRequest.GET);
    }

    @Override
    public RestfulRequest post(String uri) {
        return request(uri, RestfulRequest.POST);
    }

    @Override
    public RestfulRequest delete(String uri) {
        return request(uri, RestfulRequest.DELETE);
    }

    @Override
    public RestfulRequest head(String uri) {
        return request(uri, RestfulRequest.HEAD);
    }

    @Override
    public RestfulRequest put(String uri) {
        return request(uri, RestfulRequest.PUT);
    }

    @Override
    public RestfulRequest options(String uri) {
        return request(uri, RestfulRequest.OPTIONS);
    }

    @Override
    public RestfulRequest patch(String uri) {
        return request(uri, RestfulRequest.PATCH);
    }
}

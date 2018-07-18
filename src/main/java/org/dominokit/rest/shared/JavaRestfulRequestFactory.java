package org.dominokit.rest.shared;

import com.google.gwt.core.shared.GwtIncompatible;
import org.dominokit.rest.server.JavaRestfulRequest;

class JavaRestfulRequestFactory extends JsRestfulRequestFactory {

    @GwtIncompatible
    @Override
    RestfulRequest request(String uri, String method) {
        return new JavaRestfulRequest(uri, method);
    }

    @GwtIncompatible
    @Override
    RestfulRequest get(String uri) {
        return request(uri, RestfulRequest.GET);
    }

    @GwtIncompatible
    @Override
    RestfulRequest post(String uri) {
        return request(uri, RestfulRequest.POST);
    }

    @GwtIncompatible
    @Override
    RestfulRequest delete(String uri) {
        return request(uri, RestfulRequest.DELETE);
    }

    @GwtIncompatible
    @Override
    RestfulRequest head(String uri) {
        return request(uri, RestfulRequest.HEAD);
    }

    @GwtIncompatible
    @Override
    RestfulRequest put(String uri) {
        return request(uri, RestfulRequest.PUT);
    }

    @GwtIncompatible
    @Override
    RestfulRequest options(String uri) {
        return request(uri, RestfulRequest.OPTIONS);
    }
}

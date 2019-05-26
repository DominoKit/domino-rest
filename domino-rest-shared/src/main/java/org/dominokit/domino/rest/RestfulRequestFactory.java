package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.RestfulRequest;

public interface RestfulRequestFactory {

    RestfulRequest request(String uri, String method);

    RestfulRequest post(String uri);
    RestfulRequest get(String uri);

    RestfulRequest put(String uri);

    RestfulRequest delete(String uri);

    RestfulRequest head(String uri);

    RestfulRequest options(String uri);
    RestfulRequest patch(String uri);
}

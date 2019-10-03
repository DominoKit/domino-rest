package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.rest.shared.Response;

@FunctionalInterface
public interface ResponseReader<T> {
    T read(Response response);
}

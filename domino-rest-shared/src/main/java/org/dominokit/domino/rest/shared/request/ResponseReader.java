package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface ResponseReader<T> {
    T read(String response);
}

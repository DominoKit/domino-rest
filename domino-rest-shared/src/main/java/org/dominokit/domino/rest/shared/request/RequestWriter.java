package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface RequestWriter<T> {
    String write(T request);
}

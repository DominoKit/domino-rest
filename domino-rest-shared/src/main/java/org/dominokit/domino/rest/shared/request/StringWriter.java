package org.dominokit.domino.rest.shared.request;

public class StringWriter<T> implements RequestWriter<T> {
    @Override
    public String write(T request) {
        return String.valueOf(request);
    }
}

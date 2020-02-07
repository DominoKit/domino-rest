package org.dominokit.domino.rest.shared.request;

import java.util.function.Supplier;

public class ResponseReaderMatcher {

    private final MetaMatcher metaMatcher;
    private final Supplier<ResponseReader<?>> reader;

    ResponseReaderMatcher(MetaMatcher metaMatcher, Supplier<ResponseReader<?>> reader) {
        this.metaMatcher = metaMatcher;
        this.reader = reader;
    }

    public MetaMatcher getMetaMatcher() {
        return metaMatcher;
    }

    public ResponseReader<?> getReader() {
        return reader.get();
    }
}

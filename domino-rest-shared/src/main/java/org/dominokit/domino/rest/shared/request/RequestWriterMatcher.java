package org.dominokit.domino.rest.shared.request;

import java.util.function.Supplier;

public class RequestWriterMatcher {

    private final MetaMatcher metaMatcher;
    private final Supplier<RequestWriter<?>> writer;

    RequestWriterMatcher(MetaMatcher metaMatcher, Supplier<RequestWriter<?>> writer) {
        this.metaMatcher = metaMatcher;
        this.writer = writer;
    }

    public MetaMatcher getMetaMatcher() {
        return metaMatcher;
    }

    public RequestWriter<?> getWriter() {
        return writer.get();
    }
}

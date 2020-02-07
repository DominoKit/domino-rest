package org.dominokit.domino.rest.shared.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class CustomMappersRegistry {

    public static final CustomMappersRegistry INSTANCE = new CustomMappersRegistry();

    private static final List<RequestWriterMatcher> writerMatchers = new ArrayList<>();
    private static final List<ResponseReaderMatcher> readerMatchers = new ArrayList<>();

    private CustomMappersRegistry() {
    }

    public CustomMappersRegistry registerResponseReader(MetaMatcher matcher, Supplier<ResponseReader<?>> readerSupplier) {
        readerMatchers.add(new ResponseReaderMatcher(matcher, readerSupplier));
        return this;
    }

    public CustomMappersRegistry registerRequestWriter(MetaMatcher matcher, Supplier<RequestWriter<?>> writerSupplier) {
        writerMatchers.add(new RequestWriterMatcher(matcher, writerSupplier));
        return this;
    }

    Optional<? extends ResponseReader<?>> findReader(Request request){
        return  readerMatchers.stream()
                .filter(readers -> readers.getMetaMatcher().match(request.getMeta()))
                .map(ResponseReaderMatcher::getReader)
                .findFirst();
    }

    Optional<? extends RequestWriter<?>> findWriter(Request request){
        return  writerMatchers.stream()
                .filter(writers -> writers.getMetaMatcher().match(request.getMeta()))
                .map(RequestWriterMatcher::getWriter)
                .findFirst();
    }
}
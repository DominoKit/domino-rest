package org.dominokit.domino.rest.shared.request;

import java.util.function.Consumer;

import static java.util.Objects.isNull;

public class RequestPathHandler<R extends RequestBean, S extends ResponseBean> {
    private ServerRequest request;

    public RequestPathHandler(ServerRequest<R, S> request) {
        this.request = request;
    }

    public void process(Consumer<ServerRequest<R, S>> consumer) {
        if (isNull(request.getUrl())) {
            String serviceRoot = (isNull(request.getServiceRoot()) || request.getServiceRoot().isEmpty()) ? ServiceRootMatcher.matchedServiceRoot(request.getPath()) : request.getServiceRoot();
            request.setUrl(serviceRoot + request.getPath());
        }
        consumer.accept(request);
    }
}

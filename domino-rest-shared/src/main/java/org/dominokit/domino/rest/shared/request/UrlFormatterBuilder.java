package org.dominokit.domino.rest.shared.request;

import java.util.Map;

public class UrlFormatterBuilder<R> {
    private Map<String, String> queryParameters;
    private Map<String, String> pathParameters;
    private R requestBean;

    public UrlFormatterBuilder<R> setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public UrlFormatterBuilder<R> setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
        return this;
    }

    public UrlFormatterBuilder<R> setRequestBean(R requestBean) {
        this.requestBean = requestBean;
        return this;
    }

    public UrlFormatter<R> build() {
        return new UrlFormatter<>(queryParameters, pathParameters, requestBean);
    }
}
package org.dominokit.domino.rest.shared.request;

import java.util.Map;

public class UrlFormatterBuilder<R> {
    private Map<String, String> queryParameters;
    private Map<String, String> pathParameters;
    private Map<String, String> callArguments;
    private RequestParametersReplacer<R> requestParametersReplacer;
    private R requestBean;

    public UrlFormatterBuilder<R> setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public UrlFormatterBuilder<R> setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
        return this;
    }

    public UrlFormatterBuilder<R> setCallArguments(Map<String, String> callArguments) {
        this.callArguments = callArguments;
        return this;
    }

    public UrlFormatterBuilder<R> setRequestParametersReplacer(RequestParametersReplacer<R> requestParametersReplacer) {
        this.requestParametersReplacer = requestParametersReplacer;
        return this;
    }

    public UrlFormatterBuilder<R> setRequestBean(R requestBean) {
        this.requestBean = requestBean;
        return this;
    }

    public UrlFormatter<R> build() {
        return new UrlFormatter<>(queryParameters, pathParameters, callArguments, requestParametersReplacer, requestBean);
    }
}
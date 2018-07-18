package org.dominokit.rest.shared;

import java.util.Arrays;

import static java.util.Objects.isNull;

public abstract class BaseRestfulRequest implements RestfulRequest {

    private String uri;
    private String method;
    protected SuccessHandler successHandler;
    protected ErrorHandler errorHandler;
    private int timeout;

    public BaseRestfulRequest(String uri, String method) {
        if (isNull(uri) || uri.trim().isEmpty())
            throw new IllegalArgumentException("Invalid URI [" + uri + "]");
        if (isNull(method) || method.trim().isEmpty())
            throw new IllegalArgumentException("Invalid http method [" + method + "]");

        this.uri = uri;
        this.method = method;
    }

    @Override
    public BaseRestfulRequest addQueryString(String queryString) {
        String[] params = queryString.split("&");
        Arrays.stream(params).map(param -> param.split("=")).forEach(this::addQueryPair);
        return this;
    }

    private void addQueryPair(String[] paramNameValuePair) {
        addQueryParam(paramNameValuePair[0], paramNameValuePair[1]);
    }

    @Override
    public String getUri() {
        String queryParams = paramsAsString();
        return getPath() + (queryParams.isEmpty() ? queryParams : "?" + queryParams);
    }

    @Override
    public BaseRestfulRequest addQueryParams(String key, Iterable<String> values) {
        for (String value : values)
            addQueryParam(key, value);
        return this;
    }

    @Override
    public String getQuery() {
        return paramsAsString();
    }

    @Override
    public String getPath() {
        String path = uri;
        if (path.contains("?"))
            path = path.substring(0, path.indexOf("?"));

        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public RestfulRequest timeout(int timeout) {
        this.timeout = timeout < 0 ? 0 : timeout;
        return this;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public BaseRestfulRequest onSuccess(SuccessHandler successHandler) {
        this.successHandler = successHandler;
        return this;
    }

    @Override
    public BaseRestfulRequest onError(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    protected abstract String paramsAsString();
}

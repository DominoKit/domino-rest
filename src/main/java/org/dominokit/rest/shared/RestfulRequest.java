package org.dominokit.rest.shared;

import java.util.List;
import java.util.Map;

public interface RestfulRequest {

    String POST = "POST";
    String GET = "GET";
    String PUT = "PUT";
    String DELETE = "DELETE";
    String HEAD = "HEAD";
    String OPTIONS = "OPTIONS";
    String PATCH = "PATCH";

    static JavaRestfulRequestFactory factory() {
        return new JavaRestfulRequestFactory();
    }

    static RestfulRequest request(String uri, String method) {
        return factory().request(uri, method);
    }

    static RestfulRequest post(String uri) {
        return factory().post(uri);
    }

    static RestfulRequest get(String uri) {
        return factory().get(uri);
    }

    static RestfulRequest put(String uri) {
        return factory().put(uri);
    }

    static RestfulRequest delete(String uri) {
        return factory().delete(uri);
    }

    static RestfulRequest head(String uri) {
        return factory().head(uri);
    }

    static RestfulRequest options(String uri) {
        return factory().options(uri);
    }

    static RestfulRequest patch(String uri) {
        return factory().patch(uri);
    }


    RestfulRequest addQueryString(String queryString);

    String getUri();

    RestfulRequest addQueryParam(String key, String value);

    RestfulRequest addQueryParams(String key, Iterable<String> values);

    RestfulRequest setQueryParam(String key, String value);

    String getQuery();

    String getPath();

    String getMethod();

    RestfulRequest putHeader(String key, String value);
    RestfulRequest putHeaders(Map<String, String> headers);
    RestfulRequest putParameters(Map<String, String> parameters);

    Map<String, String> getHeaders();

    RestfulRequest timeout(int timeout);

    int getTimeout();

    void sendForm(Map<String, String> formData);

    void sendJson(String json);

    void send(String data);

    void send();

    RestfulRequest onSuccess(SuccessHandler successHandler);

    RestfulRequest onError(ErrorHandler errorHandler);

    @FunctionalInterface
    interface SuccessHandler {
        void onResponseReceived(Response response);
    }

    @FunctionalInterface
    interface ErrorHandler {
        void onError(Throwable throwable);
    }
}

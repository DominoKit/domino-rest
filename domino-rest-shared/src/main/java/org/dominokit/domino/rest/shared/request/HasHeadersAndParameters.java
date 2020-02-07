package org.dominokit.domino.rest.shared.request;

import java.util.Map;

public interface HasHeadersAndParameters<R, S> {
    HasHeadersAndParameters<R, S> setHeader(String name, String value);

    HasHeadersAndParameters<R, S> setHeaders(Map<String, String> headers);

    HasHeadersAndParameters<R, S> setQueryParameter(String name, String value);
    HasHeadersAndParameters<R, S> setQueryParameters(Map<String, String> queryParameters);

    /**
     * use {@link #setQueryParameter(String, String)}
     */
    @Deprecated
    HasHeadersAndParameters<R, S> setParameter(String name, String value);

    /**
     * use {@link #setQueryParameters(Map)}
     */
    @Deprecated

    HasHeadersAndParameters<R, S> setParameters(Map<String, String> queryParameters);

    HasHeadersAndParameters<R, S> setPathParameters(Map<String, String> pathParameters);
    HasHeadersAndParameters<R, S> setPathParameter(String name, String value);

    HasHeadersAndParameters<R, S> setHeaderParameters(Map<String, String> headerParameters);
    HasHeadersAndParameters<R, S> setHeaderParameter(String name, String value);
}

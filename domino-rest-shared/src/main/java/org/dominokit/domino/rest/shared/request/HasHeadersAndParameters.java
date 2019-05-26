package org.dominokit.domino.rest.shared.request;

import java.util.Map;

public interface HasHeadersAndParameters<R, S> {
    HasHeadersAndParameters<R, S> setHeader(String name, String value);

    HasHeadersAndParameters<R, S> setHeaders(Map<String, String> headers);

    HasHeadersAndParameters<R, S> setParameter(String name, String value);

    HasHeadersAndParameters<R, S> setParameters(Map<String, String> parameters);
}

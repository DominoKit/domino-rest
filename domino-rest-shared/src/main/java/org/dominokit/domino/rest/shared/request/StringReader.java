package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.rest.shared.Response;

public class StringReader implements ResponseReader<String> {
    @Override
    public String read(Response response) {
        return response.getBodyAsString();
    }
}

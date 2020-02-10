package org.dominokit.domino.rest.shared.request.exception;

public class QueryParameterMissingException extends RuntimeException {

    public QueryParameterMissingException(String paramName) {
        super("No parameter provided for query parameter ["+paramName+"]");
    }
}

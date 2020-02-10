package org.dominokit.domino.rest.shared.request.exception;

public class PathParameterMissingException extends RuntimeException {

    public PathParameterMissingException(String paramName) {
        super("No parameter provided for path ["+paramName+"]");
    }
}

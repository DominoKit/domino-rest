package org.dominokit.domino.rest;

public class RestfullRequestContext {

    private static RestfulRequestFactory factory;

    public static RestfulRequestFactory getFactory() {
        return factory;
    }

    static void setFactory(RestfulRequestFactory factory) {
        RestfullRequestContext.factory = factory;
    }
}

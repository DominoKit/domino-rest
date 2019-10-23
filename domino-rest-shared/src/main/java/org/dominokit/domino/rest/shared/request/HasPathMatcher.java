package org.dominokit.domino.rest.shared.request;

public interface HasPathMatcher {
    DynamicServiceRoot serviceRoot(HasServiceRoot serviceRoot);

    DynamicServiceRoot pathFormatter(PathFormatter pathFormatter);

    @FunctionalInterface
    interface HasServiceRoot {
        String onMatch();
    }

    @FunctionalInterface
    interface PathFormatter {
        String format(String root, String serviceRoot);
    }
}

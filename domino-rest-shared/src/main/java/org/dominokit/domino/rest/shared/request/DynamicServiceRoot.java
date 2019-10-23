package org.dominokit.domino.rest.shared.request;

public class DynamicServiceRoot implements HasPathMatcher {

    private PathMatcher pathMatcher;
    private HasServiceRoot hasServiceRoot;
    private PathFormatter pathFormatter = (root, serviceRoot) -> root + serviceRoot;
    private String serviceRoot;

    private DynamicServiceRoot(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public boolean isMatchingPath(String serviceRoot) {
        this.serviceRoot = serviceRoot;
        return pathMatcher.isMatch(serviceRoot);
    }

    public String onMatchingPath() {
        String root = hasServiceRoot.onMatch();
        return pathFormatter.format(root, serviceRoot);
    }

    public static HasPathMatcher pathMatcher(PathMatcher pathMatcher) {
        return new DynamicServiceRoot(pathMatcher);
    }

    @Override
    public DynamicServiceRoot serviceRoot(HasServiceRoot hasServiceRoot) {
        this.hasServiceRoot = hasServiceRoot;
        return this;
    }

    public DynamicServiceRoot pathFormatter(PathFormatter pathFormatter) {
        this.pathFormatter = pathFormatter;
        return this;
    }

    @FunctionalInterface
    public interface PathMatcher {
        boolean isMatch(String path);
    }
}

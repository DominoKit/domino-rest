package org.dominokit.domino.rest.shared.request;

public class DynamicServiceRoot implements HasPathMatcher{

    private PathMatcher pathMatcher;
    private HasServiceRoot serviceRoot;

    private DynamicServiceRoot(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    public boolean isMatchingPath(String path){
        return pathMatcher.isMatch(path);
    }

    public String onMatchingPath(){
        return serviceRoot.onMatch();
    }

    public static HasPathMatcher pathMatcher(PathMatcher pathMatcher){
        return new DynamicServiceRoot(pathMatcher);
    }

    @Override
    public DynamicServiceRoot serviceRoot(HasServiceRoot serviceRoot) {
        this.serviceRoot=serviceRoot;
        return this;
    }

    @FunctionalInterface
    public interface PathMatcher{
        boolean isMatch(String path);
    }

    @FunctionalInterface
    public interface HasServiceRoot{
        String onMatch();
    }

}

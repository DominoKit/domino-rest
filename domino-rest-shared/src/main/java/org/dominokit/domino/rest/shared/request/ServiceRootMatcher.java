package org.dominokit.domino.rest.shared.request;

import java.util.List;

public class ServiceRootMatcher {

    private static final DynamicServiceRoot defaultRoot = DynamicServiceRoot.pathMatcher(path -> true)
            .serviceRoot(() -> RequestContext.make().getConfig().getDefaultServiceRoot());

    public static String matchedServiceRoot(String path) {
        final List<DynamicServiceRoot> serviceRoots = RequestContext.make().getConfig().getServiceRoots();
        return serviceRoots.stream().filter(r -> r.isMatchingPath(path)).findFirst().orElse(defaultRoot).onMatchingPath();
    }

    public static boolean hasServiceRoot(String path){
        final List<DynamicServiceRoot> serviceRoots = RequestContext.make().getConfig().getServiceRoots();
        return serviceRoots.stream().anyMatch(r -> r.isMatchingPath(path));
    }
}

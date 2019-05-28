package org.dominokit.domino.rest;

import org.dominokit.domino.rest.gwt.DefaulServiceRoot;
import org.dominokit.domino.rest.gwt.GwtRequestAsyncSender;
import org.dominokit.domino.rest.gwt.ServerEventFactory;
import org.dominokit.domino.rest.shared.request.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DominoRestConfig implements RestConfig {

    private static String defaultServiceRoot;
    private static String defaultResourceRootPath = "service";
    private static String defaultJsonDateFormat = null;

    private static final RequestRouter<ServerRequest> serverRouter = new ServerRouter(new GwtRequestAsyncSender(new ServerEventFactory()));
    private static List<DynamicServiceRoot> dynamicServiceRoots = new ArrayList<>();
    private static final List<RequestInterceptor> interceptors = new ArrayList<>();

    public static DominoRestConfig initDefaults() {
        RestfullRequestContext.setFactory(new JsRestfulRequestFactory());
        DominoRestContext.init(DominoRestConfig.getInstance());
        return DominoRestConfig.getInstance();
    }

    public static DominoRestConfig getInstance() {
        return new DominoRestConfig();
    }

    public DominoRestConfig setDefaultServiceRoot(String defaultServiceRoot) {
        this.defaultServiceRoot = defaultServiceRoot;
        return this;
    }

    public DominoRestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat) {
        this.defaultJsonDateFormat = defaultJsonDateFormat;
        return this;
    }

    public DominoRestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot) {
        dynamicServiceRoots.add(dynamicServiceRoot);
        return this;
    }

    public DominoRestConfig addRequestInterceptor(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    public DominoRestConfig removeRequestInterceptor(RequestInterceptor interceptor) {
        this.interceptors.remove(interceptor);
        return this;
    }

    public List<RequestInterceptor> getRequestInterceptors() {
        return interceptors;
    }

    public String getDefaultServiceRoot() {
        if (isNull(defaultServiceRoot)) {
            return DefaulServiceRoot.get() + defaultResourceRootPath+"/";
        }
        return defaultServiceRoot;
    }

    public String getDefaultJsonDateFormat() {
        return defaultJsonDateFormat;
    }

    public List<DynamicServiceRoot> getServiceRoots() {
        return dynamicServiceRoots;
    }

    public DominoRestConfig setDefaultResourceRootPath(String rootPath) {
        if (nonNull(rootPath)) {
            this.defaultResourceRootPath = rootPath;
        }
        return this;
    }

    public RequestRouter<ServerRequest> getServerRouter() {
        return serverRouter;
    }

    public String getDefaultResourceRootPath() {
        return defaultResourceRootPath;
    }

    @Override
    public AsyncRunner asyncRunner() {
        return asyncTask -> {
            try {
                asyncTask.onSuccess();
            } catch (Throwable error) {
                asyncTask.onFailed(error);
            }
        };
    }
}
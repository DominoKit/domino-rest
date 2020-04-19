package org.dominokit.domino.rest;

import org.dominokit.domino.rest.gwt.DefaultServiceRoot;
import org.dominokit.domino.rest.gwt.ServerEventFactory;
import org.dominokit.domino.rest.shared.request.*;
import org.dominokit.jacksonapt.JacksonContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DominoRestConfig implements RestConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DominoRestConfig.class);

    private static String defaultServiceRoot;
    private static String defaultResourceRootPath = "service";
    private static String defaultJsonDateFormat = null;

    private static final RequestRouter<ServerRequest> serverRouter = new ServerRouter(
            new DefaultRequestAsyncSender(new ServerEventFactory(), new RequestSender<>()));
    private static List<DynamicServiceRoot> dynamicServiceRoots = new ArrayList<>();
    private static final List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private static final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();

    private static Fail defaultFailHandler = failedResponse -> {
        if (nonNull(failedResponse.getThrowable())) {
            LOGGER.debug("could not execute request on server: ", failedResponse.getThrowable());
        } else {
            LOGGER.debug("could not execute request on server: status [" + failedResponse.getStatusCode() + "], body [" + failedResponse.getBody() + "]");
        }
    };

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
        JacksonContextProvider.get().defaultDeserializerParameters()
                .setPattern(defaultJsonDateFormat);
        return this;
    }

    public DominoRestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot) {
        dynamicServiceRoots.add(dynamicServiceRoot);
        return this;
    }

    public DominoRestConfig addRequestInterceptor(RequestInterceptor interceptor) {
        this.requestInterceptors.add(interceptor);
        return this;
    }

    public DominoRestConfig removeRequestInterceptor(RequestInterceptor interceptor) {
        this.requestInterceptors.remove(interceptor);
        return this;
    }

    public List<RequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }

    public DominoRestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor){
        this.getResponseInterceptors().add(responseInterceptor);
        return this;
    }

    public DominoRestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor){
        this.getResponseInterceptors().remove(responseInterceptor);
        return this;
    }

    @Override
    public List<ResponseInterceptor> getResponseInterceptors() {
        return responseInterceptors;
    }

    public DominoRestConfig setDefaultFailHandler(Fail fail){
        if(nonNull(fail)){
            this.defaultFailHandler = fail;
        }
        return this;
    }

    @Override
    public Fail getDefaultFailHandler() {
        return defaultFailHandler;
    }

    public String getDefaultServiceRoot() {
        if (isNull(defaultServiceRoot)) {
            return DefaultServiceRoot.get() + defaultResourceRootPath + "/";
        }
        return defaultServiceRoot;
    }

    public String getDefaultJsonDateFormat() {
        return JacksonContextProvider.get().defaultDeserializerParameters()
                .getPattern();
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
        if (nonNull(defaultResourceRootPath) && !defaultResourceRootPath.trim().isEmpty()) {
            return defaultResourceRootPath + "/";
        } else {
            return "";
        }
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
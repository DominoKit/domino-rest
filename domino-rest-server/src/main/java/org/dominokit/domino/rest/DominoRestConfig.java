package org.dominokit.domino.rest;

import org.dominokit.domino.rest.server.DefaultServiceRoot;
import org.dominokit.domino.rest.server.OnServerRequestEventFactory;
import org.dominokit.domino.rest.shared.request.*;
import org.dominokit.jacksonapt.JacksonContextProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DominoRestConfig implements RestConfig {

    private static final Logger LOGGER = Logger.getLogger(DominoRestConfig.class.getName());

    private static String defaultServiceRoot;
    private static String defaultResourceRootPath = "service";
    private static String defaultJsonDateFormat = null;

    private static RequestRouter<ServerRequest> serverRouter = new ServerRouter(
            new DefaultRequestAsyncSender(new OnServerRequestEventFactory(), new RequestSender<>()));
    private static List<DynamicServiceRoot> dynamicServiceRoots = new ArrayList<>();
    private static List<RequestInterceptor> requestInterceptors = new ArrayList<>();
    private static final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();
    private static Fail defaultFailHandler = failedResponse -> {
        if (nonNull(failedResponse.getThrowable())) {
            LOGGER.log(Level.FINE, "could not execute request on server: ", failedResponse.getThrowable());
        } else {
            LOGGER.log(Level.FINE, "could not execute request on server: status [" + failedResponse.getStatusCode() + "], body [" + failedResponse.getBody() + "]");
        }
    };

    public static DominoRestConfig initDefaults() {
        RestfullRequestContext.setFactory(new JavaRestfulRequestFactory());
        DominoRestContext.init(DominoRestConfig.getInstance());
        return DominoRestConfig.getInstance();
    }

    public static DominoRestConfig getInstance() {
        return new DominoRestConfig();
    }

    @Override
    public DominoRestConfig setDefaultServiceRoot(String defaultServiceRoot) {
        this.defaultServiceRoot = defaultServiceRoot;
        return this;
    }

    @Override
    public DominoRestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat) {
        JacksonContextProvider.get().defaultDeserializerParameters()
                .setPattern(defaultJsonDateFormat);
        return this;
    }

    @Override
    public DominoRestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot) {
        dynamicServiceRoots.add(dynamicServiceRoot);
        return this;
    }

    @Override
    public DominoRestConfig addRequestInterceptor(RequestInterceptor interceptor) {
        this.requestInterceptors.add(interceptor);
        return this;
    }

    @Override
    public DominoRestConfig removeRequestInterceptor(RequestInterceptor interceptor) {
        this.requestInterceptors.remove(interceptor);
        return this;
    }

    @Override
    public List<RequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }


    @Override
    public DominoRestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor){
        this.getResponseInterceptors().add(responseInterceptor);
        return this;
    }

    @Override
    public DominoRestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor){
        this.getResponseInterceptors().remove(responseInterceptor);
        return this;
    }

    @Override
    public List<ResponseInterceptor> getResponseInterceptors() {
        return responseInterceptors;
    }

    @Override
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

    @Override
    public String getDefaultServiceRoot() {
        if (isNull(defaultServiceRoot)) {
            return DefaultServiceRoot.get() + defaultResourceRootPath + "/";
        }
        return defaultServiceRoot;
    }

    @Override
    public String getDefaultJsonDateFormat() {
        return JacksonContextProvider.get().defaultDeserializerParameters().getPattern();
    }

    @Override
    public List<DynamicServiceRoot> getServiceRoots() {
        return dynamicServiceRoots;
    }

    @Override
    public DominoRestConfig setDefaultResourceRootPath(String rootPath) {
        if (nonNull(rootPath)) {
            this.defaultResourceRootPath = rootPath;
        }
        return this;
    }

    @Override
    public RequestRouter<ServerRequest> getServerRouter() {
        return serverRouter;
    }

    @Override
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

    public void setServerRouter(RequestRouter<ServerRequest> serverRouter) {
        DominoRestConfig.serverRouter = serverRouter;
    }
}
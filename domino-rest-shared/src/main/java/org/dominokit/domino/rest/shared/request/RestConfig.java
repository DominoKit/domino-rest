package org.dominokit.domino.rest.shared.request;

import java.util.Date;
import java.util.List;

public interface RestConfig {
    RestConfig setDefaultResourceRootPath(String rootPath);

    RequestRouter<ServerRequest> getServerRouter();

    String getDefaultServiceRoot();

    String getDefaultJsonDateFormat();

    List<DynamicServiceRoot> getServiceRoots();

    RestConfig setDefaultServiceRoot(String defaultServiceRoot);

    RestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat);

    RestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot);

    RestConfig addRequestInterceptor(RequestInterceptor interceptor);

    RestConfig removeRequestInterceptor(RequestInterceptor interceptor);

    List<RequestInterceptor> getRequestInterceptors();

    RestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor);

    RestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor);

    List<ResponseInterceptor> getResponseInterceptors();

    String getDefaultResourceRootPath();

    RestConfig setDefaultFailHandler(Fail fail);

    Fail getDefaultFailHandler();

    AsyncRunner asyncRunner();

    RestConfig setDateParamFormatter(DateParamFormatter formatter);
    DateParamFormatter getDateParamFormatter();

    @FunctionalInterface
    interface DateParamFormatter {
        String format(Date date, String pattern);
    }
}

package org.dominokit.domino.rest.shared.request;

public class RequestContext {

    private static RequestConfig config;
    private RequestContext(){}

    public static void init(RequestConfig hasRouters){
        RequestContext.config = hasRouters;
    }

    public static RequestContext make(){
        return new RequestContext();
    }

    public RequestConfig getConfig(){
        return config;
    }
}

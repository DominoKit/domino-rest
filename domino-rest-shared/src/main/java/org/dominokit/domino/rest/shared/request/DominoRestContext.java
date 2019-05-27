package org.dominokit.domino.rest.shared.request;

public class DominoRestContext {

    private static RestConfig config;
    private DominoRestContext(){}

    public static void init(RestConfig hasRouters){
        DominoRestContext.config = hasRouters;
    }

    public static DominoRestContext make(){
        return new DominoRestContext();
    }

    public RestConfig getConfig(){
        return config;
    }
}

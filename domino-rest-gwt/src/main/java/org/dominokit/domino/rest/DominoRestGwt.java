package org.dominokit.domino.rest;

import com.google.gwt.core.client.EntryPoint;
import org.dominokit.domino.rest.shared.request.RequestContext;

public class DominoRestGwt implements EntryPoint {
    @Override
    public void onModuleLoad() {
        RestfullRequestContext.setFactory(new JsRestfulRequestFactory());
        RequestContext.init(GwtRequestConfig.getInstance());
    }
}

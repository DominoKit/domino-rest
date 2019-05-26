package org.dominokit.domino.rest.gwt;

import org.dominokit.domino.rest.shared.request.*;

public class GwtRequestAsyncSender extends AbstractRequestAsyncSender {

    public GwtRequestAsyncSender(ServerRequestEventFactory requestEventFactory) {
        super(requestEventFactory);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void sendRequest(ServerRequest request, ServerRequestEventFactory requestEventFactory) {
        request.getSender()
                .send(request,
                        new ServerRequestCallBack() {

                            @Override
                            public <T> void onSuccess(T response) {
                                requestEventFactory.makeSuccess(request, response).fire();
                            }

                            @Override
                            public void onFailure(FailedResponseBean failedResponse) {
                                requestEventFactory.makeFailed(request, failedResponse).fire();
                            }
                        });
    }
}

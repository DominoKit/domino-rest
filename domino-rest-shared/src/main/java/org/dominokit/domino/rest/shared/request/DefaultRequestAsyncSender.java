package org.dominokit.domino.rest.shared.request;

public class DefaultRequestAsyncSender extends AbstractRequestAsyncSender {

    private final RequestRestSender requestSender;

    public DefaultRequestAsyncSender(ServerRequestEventFactory requestEventFactory, RequestRestSender requestSender) {
        super(requestEventFactory);
        this.requestSender = requestSender;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void sendRequest(ServerRequest request, ServerRequestEventFactory requestEventFactory) {
        requestSender.send(request,
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

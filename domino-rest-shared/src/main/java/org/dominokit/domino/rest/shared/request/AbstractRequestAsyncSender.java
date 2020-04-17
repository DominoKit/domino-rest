package org.dominokit.domino.rest.shared.request;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractRequestAsyncSender implements RequestAsyncSender {

    private static final Logger LOGGER = Logger.getLogger(RequestAsyncSender.class.getName());
    private final ServerRequestEventFactory requestEventFactory;

    public AbstractRequestAsyncSender(ServerRequestEventFactory requestEventFactory) {
        this.requestEventFactory = requestEventFactory;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final void send(final ServerRequest request) {
        DominoRestContext.make().getConfig().asyncRunner().runAsync(new RequestAsyncTask(request));
    }

    private class RequestAsyncTask implements AsyncRunner.AsyncTask {
        private final ServerRequest request;

        private RequestAsyncTask(ServerRequest request) {
            this.request = request;
        }

        @Override
        public void onSuccess() {
            sendRequest(request, requestEventFactory);
        }

        @Override
        public void onFailed(Throwable error) {
            LOGGER.log(Level.SEVERE, "Could not RunAsync request [" + request + "]", error);
        }
    }

    protected abstract void sendRequest(ServerRequest request, ServerRequestEventFactory requestEventFactory);
}

package org.dominokit.domino.rest.shared.request;

public interface RequestRestSender<T, S > {
    void send(ServerRequest<T, S> request, ServerRequestCallBack callBack);
}

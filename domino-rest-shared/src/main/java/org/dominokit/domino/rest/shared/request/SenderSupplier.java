package org.dominokit.domino.rest.shared.request;

import java.util.Objects;
import java.util.function.Supplier;

public class SenderSupplier<R,S> implements Supplier<RequestRestSender<R,S>> {

    private RequestRestSender<R,S> sender;
    private final Supplier<RequestRestSender<R,S>> senderFactory;

    public SenderSupplier(Supplier<RequestRestSender<R,S>> senderFactory) {
        this.senderFactory = senderFactory;
    }

    @Override
    public RequestRestSender<R,S> get(){
        if(Objects.isNull(sender))
            this.sender=senderFactory.get();
        return sender;
    }
}

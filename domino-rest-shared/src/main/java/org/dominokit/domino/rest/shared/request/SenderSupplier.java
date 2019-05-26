package org.dominokit.domino.rest.shared.request;

import java.util.Objects;
import java.util.function.Supplier;

public class SenderSupplier implements Supplier<RequestRestSender> {

    private RequestRestSender sender;
    private final Supplier<RequestRestSender> senderFactory;

    public SenderSupplier(Supplier<RequestRestSender> senderFactory) {
        this.senderFactory = senderFactory;
    }

    @Override
    public RequestRestSender get(){
        if(Objects.isNull(sender))
            this.sender=senderFactory.get();
        return sender;
    }
}

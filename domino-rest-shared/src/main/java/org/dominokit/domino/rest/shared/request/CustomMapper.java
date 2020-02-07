package org.dominokit.domino.rest.shared.request;

import java.util.function.Supplier;

public class CustomMapper {

    private MetaMatcher matcher;

    private CustomMapper(MetaMatcher matcher) {
        this.matcher = matcher;
    }

    public static CustomMapper matcher(MetaMatcher matcher){
        return new CustomMapper(matcher);
    }

    public CustomMapper reader(Supplier<ResponseReader<?>> reader){
        CustomMappersRegistry.INSTANCE.registerResponseReader(matcher, reader);
        return this;
    }

    public CustomMapper writer(Supplier<RequestWriter<?>> writer){
        CustomMappersRegistry.INSTANCE.registerRequestWriter(matcher, writer);
        return this;
    }
}

package org.dominokit.domino.rest.shared;

@FunctionalInterface
public interface EventsBus<T>{

    @FunctionalInterface
    interface RequestEvent<T>{
        T asEvent();
    }

    void publishEvent(RequestEvent<T> event);
}

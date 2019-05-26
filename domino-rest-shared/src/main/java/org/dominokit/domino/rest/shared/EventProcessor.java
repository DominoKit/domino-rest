package org.dominokit.domino.rest.shared;

@FunctionalInterface
public interface EventProcessor {
    void process(Event event);
}

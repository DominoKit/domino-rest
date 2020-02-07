package org.dominokit.domino.rest.shared.request;

@FunctionalInterface
public interface MetaMatcher {
    boolean match(RequestMeta meta);
}

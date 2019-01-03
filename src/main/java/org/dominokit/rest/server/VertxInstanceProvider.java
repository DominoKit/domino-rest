package org.dominokit.rest.server;

import io.vertx.core.Vertx;

public interface VertxInstanceProvider {
    Vertx getInstance();
}

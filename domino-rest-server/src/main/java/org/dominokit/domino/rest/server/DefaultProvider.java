package org.dominokit.domino.rest.server;

import io.vertx.core.Vertx;
import org.dominokit.domino.rest.VertxInstanceProvider;

public class DefaultProvider implements VertxInstanceProvider {
    @Override
    public Vertx getInstance() {
        return Vertx.vertx();
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public int getPort() {
        return 8080;
    }

    @Override
    public String getProtocol() {
        return "http";
    }
}

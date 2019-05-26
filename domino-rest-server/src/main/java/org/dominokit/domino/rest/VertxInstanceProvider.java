package org.dominokit.domino.rest;

import io.vertx.core.Vertx;
import org.dominokit.domino.rest.shared.GwtIncompatible;

@GwtIncompatible
public interface VertxInstanceProvider {
    Vertx getInstance();
}

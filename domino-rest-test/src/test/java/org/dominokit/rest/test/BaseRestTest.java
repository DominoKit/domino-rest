/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxTestContext;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.dominokit.rest.DominoRestConfig;
import org.dominokit.rest.shared.request.ServerRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

public abstract class BaseRestTest {

  private static final Logger LOGGER =
      Logger.getLogger(QueryParamTypesTest.class.getCanonicalName());
  protected static int port = 0;
  protected static Consumer<RoutingContext> requestHandler = request -> request.response().end();
  protected static DominoRestConfig config = DominoRestConfig.initDefaults();
  protected static HttpServer httpServer;
  private static Router router;

  @BeforeAll
  @DisplayName("Starting server")
  static void startServer(Vertx vertx, VertxTestContext testContext) {
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router
        .route()
        .handler(
            event -> {
              LOGGER.info("Received Request :[" + event.request().absoluteURI() + "]");
              requestHandler.accept(event);
            });
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(
            0,
            httpServerAsyncResult -> {
              httpServer = httpServerAsyncResult.result();
              port = httpServer.actualPort();
              config.setDefaultServiceRoot("http://localhost:" + port + "/");
              LOGGER.info("Server started on port : " + port);
              testContext.completeNow();
            });
  }

  protected void sendRequest(ServerRequest<?, ?> request, Consumer<RoutingContext> handler) {
    requestHandler = handler;
    request.onSuccess(response -> {}).onComplete(() -> {}).onFailed(failedResponse -> {}).send();
  }

  @AfterAll
  public static void closeServer() {
    httpServer.close();
  }
}

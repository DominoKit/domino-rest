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
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;
import org.dominokit.rest.DominoRestConfig;
import org.dominokit.rest.model.NullQueryParamTestServiceFactory;
import org.dominokit.rest.shared.request.NullQueryParamStrategy;
import org.dominokit.rest.shared.request.ServerRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ServiceTest {

  private static int port = 0;
  private static Consumer<HttpServerRequest> requestHandler = request -> request.response().end();
  private static DominoRestConfig config = DominoRestConfig.initDefaults();
  private static HttpServer httpServer;

  @BeforeAll
  @DisplayName("Starting server")
  static void startServer(Vertx vertx, VertxTestContext testContext) {
    vertx
        .createHttpServer()
        .requestHandler(req -> requestHandler.accept(req))
        .listen(
            0,
            httpServerAsyncResult -> {
              httpServer = httpServerAsyncResult.result();
              port = httpServer.actualPort();
              config.setDefaultServiceRoot("http://localhost:" + port + "/");
              System.out.println(config.getDefaultServiceRoot());
              testContext.completeNow();
            });
  }

  @Test
  @DisplayName("NULL query param strategy is Empty")
  void nullQueryParamAsEmpty(Vertx vertx, VertxTestContext testContext) {
    DominoRestConfig.getInstance().setNullQueryParamStrategy(NullQueryParamStrategy.EMPTY);
    sendRequest(
        NullQueryParamTestServiceFactory.INSTANCE.requestWithParam(null),
        request -> {
          Assertions.assertThat(request.getParam("param1")).isEmpty();
          request.response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("NULL query param strategy is NULL")
  void nullQueryParamAsNull(Vertx vertx, VertxTestContext testContext) {
    DominoRestConfig.getInstance().setNullQueryParamStrategy(NullQueryParamStrategy.NULL);
    sendRequest(
        NullQueryParamTestServiceFactory.INSTANCE.requestWithParam(null),
        request -> {
          Assertions.assertThat(request.getParam("param1")).isEqualTo("null");
          request.response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("NULL query param strategy is OMIT")
  void nullQueryParamAsOmit(Vertx vertx, VertxTestContext testContext) {
    DominoRestConfig.getInstance().setNullQueryParamStrategy(NullQueryParamStrategy.OMIT);
    sendRequest(
        NullQueryParamTestServiceFactory.INSTANCE.requestWithParam(null),
        request -> {
          Assertions.assertThat(request.getParam("param1")).isNull();
          request.response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("NULL query param strategy is OMIT override global EMPTY strategy")
  void nullQueryParamAsOmitOverride(Vertx vertx, VertxTestContext testContext) {
    DominoRestConfig.getInstance().setNullQueryParamStrategy(NullQueryParamStrategy.EMPTY);
    sendRequest(
        NullQueryParamTestServiceFactory.INSTANCE.overrideStrategy(null),
        request -> {
          Assertions.assertThat(request.getParam("param1")).isNull();
          request.response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  private void sendRequest(ServerRequest<?, ?> request, Consumer<HttpServerRequest> handler) {
    requestHandler = handler;
    request.onSuccess(response -> {}).onComplete(() -> {}).onFailed(failedResponse -> {}).send();
  }

  @AfterAll
  public static void closeServer() {
    httpServer.close();
  }
}

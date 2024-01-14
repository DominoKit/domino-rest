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

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import jakarta.ws.rs.core.MediaType;
import org.dominokit.rest.model.ChildProduceConsumesTestServiceFactory;
import org.dominokit.rest.model.ChildProduceConsumesTestService_NestedFactory;
import org.dominokit.rest.model.ProduceConsumesTestServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ProducesConsumesTest extends BaseRestTest {

  @Test
  @DisplayName("Produces JSON and Consumes Text defined on service method")
  void produceJsonConsumesText(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ProduceConsumesTestServiceFactory.INSTANCE.produceJsonConsumesText("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.TEXT_PLAIN);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.APPLICATION_JSON);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Produces Text and Consumes JSON defined on service method")
  void produceTextConsumesJson(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ProduceConsumesTestServiceFactory.INSTANCE.produceTextConsumesJson("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.APPLICATION_JSON);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Produces Text defined on service method and Consumes Text defined on interface")
  void produceTextConsumesTextGlobal(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ProduceConsumesTestServiceFactory.INSTANCE.produceTextConsumesTextGlobal("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.TEXT_PLAIN);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Produces Json defined on interface and Consumes Json defined on service method")
  void produceJsonGlobalConsumesJson(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ProduceConsumesTestServiceFactory.INSTANCE.produceJsonGlobalConsumesJson("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.APPLICATION_JSON);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.APPLICATION_JSON);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName(
      "Child service inherited method Produces Json defined on interface and Consumes Json defined interface")
  void childProduceJsonGlobalConsumesJson(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ChildProduceConsumesTestServiceFactory.INSTANCE.produceJsonGlobalConsumesJson("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.APPLICATION_JSON);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Child service Produces Text defined on interface and Consumes JSON as default")
  void childProduceTextConsumesText(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ChildProduceConsumesTestServiceFactory.INSTANCE.childProduceTextConsumesText("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.APPLICATION_JSON);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName(
      "Child service Produces Text defined on interface and Consumes Text defined on method")
  void childProduceTextConsumesJson(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ChildProduceConsumesTestServiceFactory.INSTANCE.childProduceTextConsumesJson("value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.TEXT_PLAIN);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName(
      "Child service Produces Text defined on enclosing interface and Consumes html defined on interface")
  void nestedProduceTextConsumesHtml(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        ChildProduceConsumesTestService_NestedFactory.INSTANCE.nestedProduceTextConsumesHtml(
            "value1"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().getHeader("Content-Type"))
                    .contains(MediaType.TEXT_HTML);
                assertThat(routingContext.request().getHeader("accept"))
                    .contains(MediaType.TEXT_PLAIN);
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }
}

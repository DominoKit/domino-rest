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
import static org.dominokit.rest.model.QueryParameterTestService.*;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.dominokit.rest.model.QueryParameterTestServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class QueryParamTypesTest extends BaseRestTest {

  @Test
  @DisplayName("Different query params")
  void differentParams(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.differentParams("value1", "value2"),
        routingContext -> {
          assertThat(routingContext.request().getParam("param1")).isEqualTo("value1");
          assertThat(routingContext.request().getParam("param2")).isEqualTo("value2");
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Multiple query params with same name")
  void multiSameParamParams(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.multiSameParamParams(
            "value1-1", "value1-2", "value2"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().params().getAll("param1"))
                    .containsOnly("value1-1", "value1-2");
                assertThat(routingContext.request().params().getAll("param2"))
                    .containsOnly("value2");
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Same date query params with same name and different formats")
  void sameDateParamsWithDifferentFormats(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.multiSameDateParamParams(
            new Date(), new Date(), "value2"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().params().getAll("param1"))
                    .containsOnly(
                        new SimpleDateFormat("dd-MM-yy").format(new Date()),
                        new SimpleDateFormat("dd-M-yy").format(new Date()));
                assertThat(routingContext.request().params().getAll("param2"))
                    .containsOnly("value2");
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Query param as a collection")
  void queryParamAsaCollection(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.paramsList(
            Arrays.asList("value1-1", "value1-2", "value1-3"), "value2"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().params().getAll("param1"))
                    .containsOnly("value1-1", "value1-2", "value1-3");
                assertThat(routingContext.request().params().getAll("param2"))
                    .containsOnly("value2");
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Query param as a collection with header param")
  void queryParamAsCollectionWithPathParam(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.paramsListWithPathParam(
            "path1Value", Arrays.asList("value1-1", "value1-2", "value1-3"), "value2"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().absoluteURI())
                    .endsWith(
                        "test/path1Value?param1=value1-1&param1=value1-2&param1=value1-3&param2=value2");
                assertThat(routingContext.request().params().getAll("param1"))
                    .containsOnly("value1-1", "value1-2", "value1-3");
                assertThat(routingContext.request().params().getAll("param2"))
                    .containsOnly("value2");
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }

  @Test
  @DisplayName("Query param as Enum collection")
  void queryParamAsEnumCollection(Vertx vertx, VertxTestContext testContext) {
    sendRequest(
        QueryParameterTestServiceFactory.INSTANCE.enumParamsList(
            Arrays.asList(SampleEnum.A, SampleEnum.B, SampleEnum.C), "value2"),
        routingContext -> {
          testContext.verify(
              () -> {
                assertThat(routingContext.request().params().getAll("param1"))
                    .containsOnly("A", "B", "C");
                assertThat(routingContext.request().params().getAll("param2"))
                    .containsOnly("value2");
              });
          routingContext.request().response().end(Json.encode(0));
          testContext.completeNow();
        });
  }
}

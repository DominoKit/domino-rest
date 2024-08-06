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
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.dominokit.rest.model.JakartaResponseServiceFactory;
import org.dominokit.rest.shared.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ResponseReturnTypeTest extends BaseRestTest {

  @Test
  @DisplayName("Test using jakarta.ws.rs.core.Response as a return type")
  void nullQueryParamAsEmpty(Vertx vertx, VertxTestContext testContext) {
    JakartaResponseServiceFactory.INSTANCE
        .getResponse()
        .onSuccess(
            response -> {
              assertThat(Response.class.isAssignableFrom(response.getClass()));
              testContext.completeNow();
            })
        .send();
  }
}

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
package org.dominokit.rest.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.dominokit.rest.processor.resources.FormParamsResource;
import org.dominokit.rest.shared.UrlEncodedForm;
import org.dominokit.rest.shared.request.ServerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

public class FormParamsIntegrationTest extends IntegrationTestBase {

  @Override
  protected void registerResources(ResourceConfig config) {
    config.register(FormParamsResource.class);
  }

  @Test
  void sendsUrlEncodedFormBody() throws Exception {
    ServerRequest<UrlEncodedForm, TestResponse> request =
        FormParamsServiceFactory.INSTANCE.login("a b@example.com", "secret", null);
    assertThat(request.headers().get("Content-type")).contains("application/x-www-form-urlencoded");

    Result<TestResponse> result = executeRequest(request.setServiceRoot(baseUrl));

    assertTrue(result.isSuccess());
    assertThat(result.successBody.getQueryParameters().get("email"))
        .containsExactly("a b@example.com");
    assertThat(result.successBody.getQueryParameters().get("password")).containsExactly("secret");
    assertThat(result.successBody.getQueryParameters().get("asuser")).containsExactly("<absent>");
  }

  @Test
  void formParamWithoutConsumesStillPostsForm() throws Exception {
    Result<TestResponse> result =
        executeRequest(
            FormParamsServiceFactory.INSTANCE.plain("user@example.com").setServiceRoot(baseUrl));

    assertTrue(result.isSuccess());
    assertThat(result.successBody.getQueryParameters().get("email"))
        .containsExactly("user@example.com");
  }

  @Test
  void repeatsCollectionValues() throws Exception {
    Result<TestResponse> result =
        executeRequest(
            FormParamsServiceFactory.INSTANCE
                .ids(Arrays.asList(1L, 2L), 2)
                .setServiceRoot(baseUrl));

    assertTrue(result.isSuccess());
    assertThat(result.successBody.getQueryParameters().get("id")).containsExactly("1,2");
    assertThat(result.successBody.getQueryParameters().get("count")).containsExactly("2");
  }
}

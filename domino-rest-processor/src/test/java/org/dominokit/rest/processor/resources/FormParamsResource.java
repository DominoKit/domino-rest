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
package org.dominokit.rest.processor.resources;

import jakarta.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.dominokit.rest.processor.FormParamsService;
import org.dominokit.rest.processor.TestResponse;

@Path("form-test")
public class FormParamsResource implements FormParamsService {

  @Override
  public TestResponse login(String email, String password, String asUser) {
    return TestResponse.make()
        .addQueryParameter("email", email)
        .addQueryParameter("password", password)
        .addQueryParameter("asuser", asUser == null ? "<absent>" : asUser);
  }

  @Override
  public TestResponse plain(String email) {
    return TestResponse.make().addQueryParameter("email", email);
  }

  @Override
  public TestResponse ids(List<Long> ids, int count) {
    String joined =
        ids == null
            ? "<absent>"
            : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    return TestResponse.make()
        .addQueryParameter("id", joined)
        .addQueryParameter("count", String.valueOf(count));
  }
}

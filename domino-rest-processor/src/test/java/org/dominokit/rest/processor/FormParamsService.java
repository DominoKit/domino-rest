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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

@RequestFactory
@Path("form-test")
public interface FormParamsService {

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse login(
      @FormParam("email") String email,
      @FormParam("password") String password,
      @FormParam("asuser") String asUser);

  @POST
  @Path("plain")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse plain(@FormParam("email") String email);

  @POST
  @Path("ids")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse ids(@FormParam("id") List<Long> ids, @FormParam("count") int count);
}

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
package org.dominokit.rest.model;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

@RequestFactory
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.TEXT_PLAIN)
public interface ProduceConsumesTestService {

  @GET
  @Path("test")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.TEXT_PLAIN)
  String produceJsonConsumesText(@QueryParam("param1") String param1);

  @GET
  @Path("test")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  String produceTextConsumesJson(@QueryParam("param1") String param1);

  @GET
  @Path("test")
  @Produces(MediaType.TEXT_PLAIN)
  String produceTextConsumesTextGlobal(@QueryParam("param1") String param1);

  @GET
  @Path("test")
  @Consumes(MediaType.APPLICATION_JSON)
  String produceJsonGlobalConsumesJson(@QueryParam("param1") String param1);
}

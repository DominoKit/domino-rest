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

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import org.dominokit.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

@RequestFactory
public interface PickBodyService {

  @Path("/")
  String methoda(SampleDTO sampleDTO);

  @Path("/")
  String methodb(String test, SampleDTO sampleDTO);

  @Path("/")
  String methodc(String test, String test2);

  @Path("/{test}/{test3}")
  String methodd(
      @PathParam("test") String test,
      @HeaderParam("test5") String test5,
      @QueryParam("test2") String test2,
      @Context String test3,
      @Suspended String test4);

  @Path("/{test3}")
  String methode(
      String test, @QueryParam("test2") String test2, String test3, @RequestBody String test4);

  @Path("/")
  SampleDTOX methodf(SampleDTO sampleDTO);
}

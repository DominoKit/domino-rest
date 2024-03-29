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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.dominokit.rest.shared.request.service.annotations.Multipart;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

@RequestFactory
public interface MultipartTestService {

  @POST
  @Path("test")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  void textMultipart(@FormParam("id") String id, @FormParam("file") byte[] fileContent);

  @POST
  @Path("test")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  void objectMultipart(
      @FormParam("sampleObjectJson") SampleObject sampleObject,
      @FormParam("file") byte[] fileContent);

  @POST
  @Path("test")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  void wrapperMultipart(@Multipart SampleMultipartRequest request);
}

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

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.dominokit.rest.shared.request.service.annotations.Classifier;
import org.dominokit.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.rest.shared.request.service.annotations.Retries;

@Path("library/")
public interface NoFactoryAnnotationSampleService {

  @GET
  @JwtRequired
  @Path("someService/{id}")
  SampleResponse getById(@PathParam("id") @RequestBody int id, int count);

  @POST
  @Path("someService/create")
  @Produces(MediaType.APPLICATION_JSON)
  Void registerUser(@RequestBody HashMap<String, String> personalData);

  @POST
  @Path("somePath/:name")
  SampleResponse annotatedBody(SampleRequest sampleRequest);

  @POST
  @Path("somePath/{name}/{name2}")
  SampleResponse annotatedBody2(@BeanParam SampleRequest sampleRequest);

  @GET
  @Path("someService/:id")
  SampleResponse getById(String id);

  @GET
  @Path("someService/{id}")
  @Classifier("long")
  void getById(Long id);

  @GET
  @Path("someService/:id")
  @Classifier("double")
  int getById(Double id);

  @GET
  @Path("someService/:id")
  List<String> getById(double id);

  @GET
  @Path("someService/:id")
  String[] getById6(int id);

  @GET
  @Path("someService")
  String getByIdQuery(@QueryParam("userId") int id, @QueryParam("userName") String name);

  @GET
  @Path("someService/:id")
  int[] getById7(int id);

  @GET
  @Path("someService/{id}")
  int[][] getById8(int id);
  // custom reder/writer test

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  String customReaderWriter(SampleDTO sampleDTO);

  @GET
  @Path("someService/:id")
  @Retries(timeout = 3000, maxRetries = 5)
  int[][] getByI98(int id);

  @Path("path0")
  interface Int0 {
    @Path("00")
    void get0();
  }

  @Path("path1")
  interface Int1 extends Int0 {
    @Path("11")
    void get1();
  }

  @Path("path2")
  interface Int2 {
    @Path("22")
    void get2();
  }

  @Path("pathX")
  interface IntX extends Int1, Int2 {
    @Path("xx")
    void getX();
  }
}

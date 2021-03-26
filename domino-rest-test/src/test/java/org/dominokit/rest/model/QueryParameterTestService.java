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

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.dominokit.rest.shared.request.service.annotations.DateFormat;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

@RequestFactory
public interface QueryParameterTestService {

  @GET
  @Path("test")
  int differentParams(@QueryParam("param1") String param1, @QueryParam("param2") String param2);

  @GET
  @Path("test")
  int multiSameParamParams(
      @QueryParam("param1") String param1,
      @QueryParam("param1") String param12,
      @QueryParam("param2") String param2);

  @GET
  @Path("test")
  int multiSameDateParamParams(
      @QueryParam("param1") @DateFormat("dd-MM-yy") Date param1,
      @QueryParam("param1") @DateFormat("dd-M-yy") Date param12,
      @QueryParam("param2") String param2);

  @GET
  @Path("test")
  int paramsList(@QueryParam("param1") List<String> param1, @QueryParam("param2") String param2);

  @GET
  @Path("test/{path1}")
  int paramsListWithPathParam(
      @PathParam("path1") String path1,
      @QueryParam("param1") List<String> param1,
      @QueryParam("param2") String param2);

  @GET
  @Path("test")
  int enumParamsList(
      @QueryParam("param1") List<SampleEnum> param1, @QueryParam("param2") String param2);

  @GET
  @Path("test")
  int dateParamsList(
      @QueryParam("param1") @DateFormat("dd-MM-yy") Set<Date> param1,
      @QueryParam("param2") String param2);

  enum SampleEnum {
    A,
    B,
    C
  }
}

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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dominokit.rest.processor.SampleRequest;
import org.dominokit.rest.processor.TestResponse;

/** JAX-RS resource implementation for RegexPathParamsService testing. */
@Path("/regex-test")
public class RegexPathParamsResource {

  @GET
  @Path("users/{id: \\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getUserById(@PathParam("id") String id) {
    return TestResponse.make().addPathParameter("id", id);
  }

  @GET
  @Path("orders/{orderId: [a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getOrderById(@PathParam("orderId") String orderId) {
    return TestResponse.make().addPathParameter("orderId", orderId);
  }

  @GET
  @Path("products/{code: [A-Za-z0-9]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getProductByCode(@PathParam("code") String code) {
    return TestResponse.make().addPathParameter("code", code);
  }

  @GET
  @Path("store/{storeCode: [A-Z]{3}}/item/{itemId: \\d{4,8}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getStoreItem(
      @PathParam("storeCode") String storeCode, @PathParam("itemId") String itemId) {
    return TestResponse.make()
        .addPathParameter("storeCode", storeCode)
        .addPathParameter("itemId", itemId);
  }

  @GET
  @Path("contact/{email: [^@]+@[^@]+\\.[^@]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getContactByEmail(@PathParam("email") String email) {
    return TestResponse.make().addPathParameter("email", email);
  }

  @GET
  @Path("reports/{date: \\d{4}-\\d{2}-\\d{2}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getReportByDate(@PathParam("date") String date) {
    return TestResponse.make().addPathParameter("date", date);
  }

  @GET
  @Path("api/{version: v\\d+\\.\\d+\\.\\d+}/docs")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getApiDocs(@PathParam("version") String version) {
    return TestResponse.make().addPathParameter("version", version);
  }

  @GET
  @Path("blog/{slug: [a-z0-9]+(?:-[a-z0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getBlogPost(@PathParam("slug") String slug) {
    return TestResponse.make().addPathParameter("slug", slug);
  }

  @GET
  @Path("devices/{ip: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getDeviceByIp(@PathParam("ip") String ip) {
    return TestResponse.make().addPathParameter("ip", ip);
  }

  @GET
  @Path("articles/{articleId: \\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getArticleById(@PathParam("articleId") String articleId) {
    return TestResponse.make().addPathParameter("articleId", articleId);
  }

  @GET
  @Path(
      "organization/{orgId: \\d+}/department/{deptCode: [A-Z]{2,5}}/employee/{empId: [A-Z]\\d{6}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getEmployee(
      @PathParam("orgId") String orgId,
      @PathParam("deptCode") String deptCode,
      @PathParam("empId") String empId) {
    return TestResponse.make()
        .addPathParameter("orgId", orgId)
        .addPathParameter("deptCode", deptCode)
        .addPathParameter("empId", empId);
  }

  @GET
  @Path("transactions/{txnId: TX[0-9]{10}}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse getTransaction(
      @PathParam("txnId") String txnId, @QueryParam("format") String format) {
    return TestResponse.make().addPathParameter("txnId", txnId).addQueryParameter("format", format);
  }

  @POST
  @Path("accounts/{accountNum: [0-9]{8,12}}/transactions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse createTransaction(
      @PathParam("accountNum") String accountNum, SampleRequest transaction) {
    return TestResponse.make().addPathParameter("accountNum", accountNum);
  }

  @PUT
  @Path("inventory/{sku: [A-Z]{3}-\\d{4}-[A-Z]{2}}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse updateInventory(@PathParam("sku") String sku, SampleRequest inventoryData) {
    return TestResponse.make().addPathParameter("sku", sku);
  }

  @DELETE
  @Path("cache/{key: [a-zA-Z0-9_-]+}")
  @Produces(MediaType.APPLICATION_JSON)
  public TestResponse clearCache(@PathParam("key") String key) {
    return TestResponse.make().addPathParameter("key", key);
  }
}

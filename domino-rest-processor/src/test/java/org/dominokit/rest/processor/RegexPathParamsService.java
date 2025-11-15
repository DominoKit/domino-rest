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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

/**
 * Test service interface for JAX-RS regex-constrained path parameters. Path parameters can include
 * regex patterns to validate and constrain their format. Example: /users/{id: \\d+} - id must be
 * digits only
 */
@RequestFactory
@Path("/regex-test")
public interface RegexPathParamsService {

  /**
   * Test path parameter with digits-only regex constraint. Expected URL: /regex-test/users/{id:
   * \\d+} The id must match the pattern: one or more digits
   */
  @GET
  @Path("users/{id: \\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getUserById(@PathParam("id") String id);

  /**
   * Test path parameter with UUID regex pattern. Expected URL: /regex-test/orders/{orderId:
   * [a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}}
   */
  @GET
  @Path("orders/{orderId: [a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getOrderById(@PathParam("orderId") String orderId);

  /**
   * Test path parameter with alphanumeric regex. Expected URL: /regex-test/products/{code:
   * [A-Za-z0-9]+}
   */
  @GET
  @Path("products/{code: [A-Za-z0-9]+}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getProductByCode(@PathParam("code") String code);

  /**
   * Test multiple path parameters with different regex patterns. Expected URL:
   * /regex-test/store/{storeCode: [A-Z]{3}}/item/{itemId: \\d{4,8}} storeCode: exactly 3 uppercase
   * letters itemId: 4 to 8 digits
   */
  @GET
  @Path("store/{storeCode: [A-Z]{3}}/item/{itemId: \\d{4,8}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getStoreItem(
      @PathParam("storeCode") String storeCode, @PathParam("itemId") String itemId);

  /**
   * Test path parameter with email-like regex pattern. Expected URL: /regex-test/contact/{email:
   * [^@]+@[^@]+\\.[^@]+}
   */
  @GET
  @Path("contact/{email: [^@]+@[^@]+\\.[^@]+}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getContactByEmail(@PathParam("email") String email);

  /**
   * Test path parameter with date format regex (YYYY-MM-DD). Expected URL:
   * /regex-test/reports/{date: \\d{4}-\\d{2}-\\d{2}}
   */
  @GET
  @Path("reports/{date: \\d{4}-\\d{2}-\\d{2}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getReportByDate(@PathParam("date") String date);

  /**
   * Test path parameter with version number regex (semantic versioning). Expected URL:
   * /regex-test/api/{version: v\\d+\\.\\d+\\.\\d+}/docs Example: v1.2.3
   */
  @GET
  @Path("api/{version: v\\d+\\.\\d+\\.\\d+}/docs")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getApiDocs(@PathParam("version") String version);

  /**
   * Test path parameter with slug-like regex (lowercase letters, digits, hyphens). Expected URL:
   * /regex-test/blog/{slug: [a-z0-9]+(?:-[a-z0-9]+)*}
   */
  @GET
  @Path("blog/{slug: [a-z0-9]+(?:-[a-z0-9]+)*}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBlogPost(@PathParam("slug") String slug);

  /**
   * Test path parameter with IP address regex pattern. Expected URL: /regex-test/devices/{ip:
   * \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}}
   */
  @GET
  @Path("devices/{ip: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getDeviceByIp(@PathParam("ip") String ip);

  /**
   * Test alternative colon syntax with regex in path parameter. Expected URL:
   * /regex-test/articles/:articleId{\\d+}
   */
  @GET
  @Path("articles/{articleId: \\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getArticleById(@PathParam("articleId") String articleId);

  /**
   * Test complex path with multiple regex patterns and normal segments. Expected URL:
   * /regex-test/organization/{orgId: \\d+}/department/{deptCode: [A-Z]{2,5}}/employee/{empId:
   * [A-Z]\\d{6}}
   */
  @GET
  @Path(
      "organization/{orgId: \\d+}/department/{deptCode: [A-Z]{2,5}}/employee/{empId: [A-Z]\\d{6}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getEmployee(
      @PathParam("orgId") String orgId,
      @PathParam("deptCode") String deptCode,
      @PathParam("empId") String empId);

  /**
   * Test regex path parameter with query parameters. Expected URL: /regex-test/transactions/{txnId:
   * TX[0-9]{10}}?format={format}
   */
  @GET
  @Path("transactions/{txnId: TX[0-9]{10}}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getTransaction(
      @PathParam("txnId") String txnId, @QueryParam("format") String format);

  /**
   * Test POST with regex-constrained path parameter. Expected URL:
   * /regex-test/accounts/{accountNum: [0-9]{8,12}}/transactions
   */
  @POST
  @Path("accounts/{accountNum: [0-9]{8,12}}/transactions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse createTransaction(
      @PathParam("accountNum") String accountNum, SampleRequest transaction);

  /**
   * Test PUT with regex path parameter. Expected URL: /regex-test/inventory/{sku:
   * [A-Z]{3}-\\d{4}-[A-Z]{2}}
   */
  @PUT
  @Path("inventory/{sku: [A-Z]{3}-\\d{4}-[A-Z]{2}}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse updateInventory(@PathParam("sku") String sku, SampleRequest inventoryData);

  /**
   * Test DELETE with regex path parameter. Expected URL: /regex-test/cache/{key: [a-zA-Z0-9_-]+}
   */
  @DELETE
  @Path("cache/{key: [a-zA-Z0-9_-]+}")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse clearCache(@PathParam("key") String key);
}

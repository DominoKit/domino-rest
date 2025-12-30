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
import java.util.Date;
import java.util.List;
import org.dominokit.rest.shared.request.service.annotations.DateFormat;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.rest.shared.request.service.annotations.Retries;

/**
 * Test service interface combining all advanced parameter features: - Matrix parameters -
 * Regex-constrained path parameters - Query parameters - Header parameters - Fragment parameters
 * (if supported) - Different HTTP methods
 */
@RequestFactory
@Path("/advanced")
public interface AdvancedParametersService {

  /**
   * Complex GET combining regex path param, matrix params, and query params. Expected URL:
   * /advanced/api/{version: v\\d+}/resources/{id:
   * \\d+};format={format};lang={lang}?include={include}
   */
  @GET
  @Path("api/{version: v\\d+}/resources/{id: \\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse getResource(
      @PathParam("version") String version,
      @PathParam("id") String id,
      @MatrixParam("format") String format,
      @MatrixParam("lang") String lang,
      @QueryParam("include") List<String> include);

  /**
   * Test with regex path, matrix params with multiple values, and headers. Expected URL:
   * /advanced/store/{storeCode: [A-Z]{3}}/products;category={category};tag={tag1};tag={tag2}
   */
  @GET
  @Path("store/{storeCode: [A-Z]{3}}/products")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> getStoreProducts(
      @PathParam("storeCode") String storeCode,
      @MatrixParam("category") String category,
      @MatrixParam("tag") List<String> tags,
      @HeaderParam("X-Store-Region") String region);

  /**
   * POST with regex UUID path param, matrix params, and request body. Expected URL:
   * /advanced/orders/{orderId: [a-f0-9-]{36}};status={status};priority={priority}
   */
  @POST
  @Path("orders/{orderId: [a-f0-9-]{36}}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse updateOrder(
      @PathParam("orderId") String orderId,
      @MatrixParam("status") String status,
      @MatrixParam("priority") int priority,
      SampleRequest orderUpdate);

  /**
   * Test alternative colon syntax with regex, matrix params, and query params. Expected URL:
   * /advanced/catalog/:catalogId{[A-Z0-9]+}/items;filter={filter}?page={page}&size={size}
   */
  @GET
  @Path("catalog/:catalogId{[A-Z0-9]+}/items")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> getCatalogItems(
      @PathParam("catalogId") String catalogId,
      @MatrixParam("filter") String filter,
      @QueryParam("page") int page,
      @QueryParam("size") int size);

  /**
   * Complex nested path with multiple regex constraints and matrix params. Expected URL:
   * /advanced/org/{orgId: \\d+}/dept/{deptCode: [A-Z]{2,5}}/emp/{empId:
   * E\\d{6}};level={level};active={active}
   */
  @GET
  @Path("org/{orgId: \\d+}/dept/{deptCode: [A-Z]{2,5}}/emp/{empId: E\\d{6}}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse getEmployeeDetails(
      @PathParam("orgId") String orgId,
      @PathParam("deptCode") String deptCode,
      @PathParam("empId") String empId,
      @MatrixParam("level") String level,
      @MatrixParam("active") boolean active,
      @QueryParam("includeHistory") boolean includeHistory);

  /**
   * Test with date path parameter (regex), matrix params, and date query params. Expected URL:
   * /advanced/reports/{date: \\d{4}-\\d{2}-\\d{2}};format={format}?from={from}&to={to}
   */
  @GET
  @Path("reports/{date: \\d{4}-\\d{2}-\\d{2}}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse getReport(
      @PathParam("date") String date,
      @MatrixParam("format") String format,
      @QueryParam("from") @DateFormat("dd-MM-yyyy") Date from,
      @QueryParam("to") @DateFormat("dd-MM-yyyy") Date to);

  /**
   * PUT with regex path param, matrix params for versioning, and conditional headers. Expected URL:
   * /advanced/documents/{docId: DOC-\\d{8}};version={version};draft={draft}
   */
  @PUT
  @Path("documents/{docId: DOC-\\d{8}}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse updateDocument(
      @PathParam("docId") String docId,
      @MatrixParam("version") int version,
      @MatrixParam("draft") boolean draft,
      @HeaderParam("If-Match") String etag,
      SampleRequest document);

  /**
   * DELETE with complex regex path and matrix params for soft delete options. Expected URL:
   * /advanced/users/{userId: [a-zA-Z0-9_-]{8,20}};reason={reason};archive={archive}
   */
  @DELETE
  @Path("users/{userId: [a-zA-Z0-9_-]{8,20}}")
  @Produces(MediaType.APPLICATION_JSON)
  Void deleteUser(
      @PathParam("userId") String userId,
      @MatrixParam("reason") String reason,
      @MatrixParam("archive") boolean archive,
      @HeaderParam("X-Admin-Token") String adminToken);

  /**
   * Test with semantic version regex, matrix params for platform, and content negotiation. Expected
   * URL: /advanced/downloads/{version:
   * \\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?}/package;platform={platform};arch={arch}
   */
  @GET
  @Path("downloads/{version: \\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?}/package")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  byte[] downloadPackage(
      @PathParam("version") String version,
      @MatrixParam("platform") String platform,
      @MatrixParam("arch") String arch,
      @HeaderParam("Accept-Encoding") String encoding);

  /**
   * Test combining both standard JAX-RS path syntax and colon syntax with regex. Expected URL:
   * /advanced/hybrid/{region: [A-Z]{2}}/:cityCode{[A-Z]{3}}/weather;units={units}
   */
  @GET
  @Path("hybrid/{region: [A-Z]{2}}/:cityCode{[A-Z]{3}}/weather")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse getWeather(
      @PathParam("region") String region,
      @PathParam("cityCode") String cityCode,
      @MatrixParam("units") String units,
      @QueryParam("forecast") int days);

  /**
   * Test with retry configuration, regex path, matrix params, and multiple query params. Expected
   * URL: /advanced/transactions/{txnId:
   * TXN-[0-9A-F]{16}};status={status}?includeDetails={includeDetails}&format={format}
   */
  @GET
  @Path("transactions/{txnId: TXN-[0-9A-F]{16}}")
  @Produces(MediaType.APPLICATION_JSON)
  @Retries(timeout = 5000, maxRetries = 3)
  SampleResponse getTransaction(
      @PathParam("txnId") String txnId,
      @MatrixParam("status") String status,
      @QueryParam("includeDetails") boolean includeDetails,
      @QueryParam("format") String format);

  /**
   * Complex POST with email regex path param, matrix params, and file upload simulation. Expected
   * URL: /advanced/users/{email: [^@]+@[^@]+\\.[^@]+}/profile;visibility={visibility}
   */
  @POST
  @Path("users/{email: [^@]+@[^@]+\\.[^@]+}/profile")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse uploadProfile(
      @PathParam("email") String email,
      @MatrixParam("visibility") String visibility,
      @HeaderParam("Content-Type") String contentType);

  /**
   * Test PATCH with URL-safe base64 regex and matrix params for partial updates. Expected URL:
   * /advanced/resources/{resourceId: [A-Za-z0-9_-]+};fields={fields}
   */
  @PATCH
  @Path("resources/{resourceId: [A-Za-z0-9_-]+}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse patchResource(
      @PathParam("resourceId") String resourceId,
      @MatrixParam("fields") List<String> fields,
      SampleRequest partialUpdate);

  /**
   * Test with IP address regex, matrix params for filtering, and pagination. Expected URL:
   * /advanced/logs/{ip: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}};level={level}?page={page}
   */
  @GET
  @Path("logs/{ip: \\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}}")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> getLogsByIp(
      @PathParam("ip") String ip,
      @MatrixParam("level") String level,
      @QueryParam("page") int page,
      @QueryParam("limit") int limit);

  /**
   * Test HEAD request with regex path and matrix params (for checking resource metadata). Expected
   * URL: /advanced/files/{fileId: [0-9a-f]{32}};checksum={checksum}
   */
  @HEAD
  @Path("files/{fileId: [0-9a-f]{32}}")
  void checkFile(@PathParam("fileId") String fileId, @MatrixParam("checksum") String checksum);
}

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
import java.util.List;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

/** Test service interface for edge cases and special scenarios with advanced parameters. */
@RequestFactory
@Path("/edge-cases")
public interface EdgeCaseParametersService {

  /**
   * Test matrix parameter with empty/default value handling. Expected URL:
   * /edge-cases/optional;param={param}
   */
  @GET
  @Path("optional")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withOptionalMatrix(@MatrixParam("param") @DefaultValue("default") String param);

  /**
   * Test regex with special characters that need escaping. Expected URL: /edge-cases/special/{code:
   * [A-Z]+\\.[0-9]+\\.[A-Z]+} Example: ABC.123.XYZ
   */
  @GET
  @Path("special/{code: [A-Z]+\\.[0-9]+\\.[A-Z]+}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withSpecialCharsRegex(@PathParam("code") String code);

  /**
   * Test matrix parameter on root path. Expected URL: /edge-cases;rootParam={rootParam}/resources
   */
  @GET
  @Path(";rootParam={rootParam}/resources")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> withRootMatrix(@MatrixParam("rootParam") String rootParam);

  /** Test empty regex (should accept any value). Expected URL: /edge-cases/any/{value: .*} */
  @GET
  @Path("any/{value: .*}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withAnyValue(@PathParam("value") String value);

  /**
   * Test regex with grouping and alternation. Expected URL: /edge-cases/type/{resourceType:
   * (user|group|role)}
   */
  @GET
  @Path("type/{resourceType: (user|group|role)}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withAlternationRegex(@PathParam("resourceType") String resourceType);

  /**
   * Test multiple matrix parameters with same name (multi-valued). Expected URL:
   * /edge-cases/filter;tag={tag1};tag={tag2};tag={tag3}
   */
  @GET
  @Path("filter")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> withMultiValuedMatrix(@MatrixParam("tag") List<String> tags);

  /**
   * Test matrix parameter with numeric types. Expected URL: /edge-cases/range;min={min};max={max}
   */
  @GET
  @Path("range")
  @Produces(MediaType.APPLICATION_JSON)
  List<SampleResponse> withNumericMatrix(
      @MatrixParam("min") Integer min, @MatrixParam("max") Integer max);

  /**
   * Test regex with optional group. Expected URL: /edge-cases/version/{ver: v?\\d+\\.\\d+} Accepts:
   * v1.0 or 1.0
   */
  @GET
  @Path("version/{ver: v?\\d+\\.\\d+}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withOptionalRegexGroup(@PathParam("ver") String version);

  /**
   * Test regex with lookahead/lookbehind (if supported). Expected URL: /edge-cases/password/{pwd:
   * (?=.*[A-Z])(?=.*[0-9]).{8,}} Password must contain uppercase and digit, min 8 chars
   */
  @GET
  @Path("password/{pwd: (?=.*[A-Z])(?=.*[0-9]).{8,}}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withLookaheadRegex(@PathParam("pwd") String password);

  /**
   * Test colon syntax path param without regex followed by matrix params. Expected URL:
   * /edge-cases/simple/:id;meta={meta}
   */
  @GET
  @Path("simple/:id")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse colonPathWithMatrix(@PathParam("id") String id, @MatrixParam("meta") String meta);

  /**
   * Test standard path param without regex followed by matrix params. Expected URL:
   * /edge-cases/standard/{id};meta={meta}
   */
  @GET
  @Path("standard/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse standardPathWithMatrix(
      @PathParam("id") String id, @MatrixParam("meta") String meta);

  /**
   * Test matrix parameters on multiple segments. Expected URL:
   * /edge-cases/seg1;p1={p1}/seg2;p2={p2}/seg3;p3={p3}
   */
  @GET
  @Path("seg1/seg2/seg3")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse multipleSegmentMatrix(
      @MatrixParam("p1") String param1,
      @MatrixParam("p2") String param2,
      @MatrixParam("p3") String param3);

  /** Test regex that matches empty string. Expected URL: /edge-cases/optional-part/{part: \\w*} */
  @GET
  @Path("optional-part/{part: \\w*}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withOptionalRegex(@PathParam("part") String part);

  /**
   * Test very long regex pattern (boundary condition). Expected URL: /edge-cases/complex/{id:
   * [A-Z]{2}-[0-9]{4}-[A-Z]{3}-[0-9]{8}-[A-F]{4}} Example: AB-1234-XYZ-12345678-ABCD
   */
  @GET
  @Path("complex/{id: [A-Z]{2}-[0-9]{4}-[A-Z]{3}-[0-9]{8}-[A-F]{4}}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withComplexRegex(@PathParam("id") String id);

  /**
   * Test matrix param with boolean values. Expected URL:
   * /edge-cases/flags;enabled={enabled};visible={visible}
   */
  @GET
  @Path("flags")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withBooleanMatrix(
      @MatrixParam("enabled") boolean enabled, @MatrixParam("visible") boolean visible);

  /**
   * Test regex with character class negation. Expected URL: /edge-cases/exclude/{code: [^0-9]+}
   * Matches anything except digits
   */
  @GET
  @Path("exclude/{code: [^0-9]+}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withNegatedCharClass(@PathParam("code") String code);

  /**
   * Test matrix parameter combined with exact regex match (no wildcards). Expected URL:
   * /edge-cases/exact/{status: (active|inactive|pending)};priority={priority}
   */
  @GET
  @Path("exact/{status: (active|inactive|pending)}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse exactMatchWithMatrix(
      @PathParam("status") String status, @MatrixParam("priority") String priority);

  /**
   * Test path ending with matrix params (no trailing segments). Expected URL:
   * /edge-cases/terminal/{id};final={final}
   */
  @GET
  @Path("terminal/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse terminalMatrix(
      @PathParam("id") String id, @MatrixParam("final") String finalParam);

  /**
   * Test regex with Unicode character support. Expected URL: /edge-cases/unicode/{name:
   * [\\p{L}\\p{N}_-]+} Matches any Unicode letters, numbers, underscore, or hyphen
   */
  @GET
  @Path("unicode/{name: [\\p{L}\\p{N}_-]+}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withUnicodeRegex(@PathParam("name") String name);

  /**
   * Test matrix parameter with encoded special characters. Expected URL:
   * /edge-cases/encoded;param={param} param might contain URL-encoded special chars
   */
  @GET
  @Path("encoded")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withEncodedMatrix(@MatrixParam("param") String encodedParam);

  /**
   * Test regex matching only specific length. Expected URL: /edge-cases/fixed/{code: [A-Z]{5}}
   * Exactly 5 uppercase letters
   */
  @GET
  @Path("fixed/{code: [A-Z]{5}}")
  @Produces(MediaType.APPLICATION_JSON)
  SampleResponse withFixedLengthRegex(@PathParam("code") String code);
}

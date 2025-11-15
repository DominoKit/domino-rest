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
package org.dominokit.rest.client;

import com.google.gwt.junit.client.GWTTestCase;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dominokit.rest.DominoRestConfig;
import org.dominokit.rest.shared.request.DominoRestContext;
import org.dominokit.rest.shared.request.RegexValidationMode;
import org.dominokit.rest.shared.request.UrlFormatter;
import org.dominokit.rest.shared.request.exception.PathParameterMissingException;

/**
 * GWT version of UrlFormatterTests.
 *
 * <p>Covers: - Null/blank handling and no-op behavior without expressions - Replacement in PATH
 * segment names (pathParams) - Replacement in MATRIX params (names & values, matrixParams) -
 * Replacement in QUERY (names & values, queryParams) - Replacement in FRAGMENT (fragmentParams) -
 * Leading slash preservation, duplicate slashes, multiple '#' - Missing-parameter failures
 * per-context (path/matrix/query/fragment) - Mixed placeholders, mixed styles, and round-trip
 * normalization via ServicePath - Backward-compatibility constructor (single map for all) -
 * {name:regex} support and invalid-regex reporting
 */
public class UrlFormatterGwtTest extends GWTTestCase {

  private Map<String, String> pathParams;
  private Map<String, String> matrixParams;
  private Map<String, String> queryParams;
  private Map<String, String> fragmentParams;

  private UrlFormatter formatterMulti;
  private UrlFormatter formatterSingle;

  static {
    DominoRestContext.make()
        .init(new DominoRestConfig().setRegexValidationMode(RegexValidationMode.FAIL));
  }

  @Override
  public String getModuleName() {
    return "org.dominokit.rest.RestTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();

    pathParams = new HashMap<>();
    matrixParams = new HashMap<>();
    queryParams = new HashMap<>();
    fragmentParams = new HashMap<>();

    formatterMulti = new UrlFormatter(pathParams, matrixParams, queryParams, fragmentParams);
    formatterSingle = new UrlFormatter(pathParams);
  }

  public void testNullUrlThrows() {
    try {
      formatterMulti.formatUrl(null);
      fail("Expected IllegalArgumentException for null URL");
    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  public void testBlankUrlReturnsEmptyString() {
    assertEquals("", formatterMulti.formatUrl(""));
    assertEquals("", formatterMulti.formatUrl("   "));
  }

  public void testUrlWithoutExpressionsIsReturnedAsIs() {
    assertEquals("/movies/hulk", formatterMulti.formatUrl("/movies/hulk"));
    assertEquals("/movies/{hulk{", formatterMulti.formatUrl("/movies/{hulk{"));
    assertEquals("/movies/}hulk}", formatterMulti.formatUrl("/movies/}hulk}"));
  }

  public void testPathPlaceholdersBothStylesReplaced() {
    pathParams.put("name", "hulk");
    assertEquals("/movies/hulk", formatterMulti.formatUrl("/movies/{name}"));
    assertEquals("/movies/hulk", formatterMulti.formatUrl("/movies/:name"));
  }

  public void testMissingPathParamThrowsAndMentionsTokenAndContext() {
    try {
      formatterMulti.formatUrl("/movies/{name}");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{name}"));
      assertTrue(e.getMessage().contains("path"));
    }
  }

  public void testLeadingSlashPreservedNoDoubleSlash() {
    pathParams.put("id", "42");
    assertEquals("/users/42", formatterMulti.formatUrl("/users/{id}"));
    assertEquals("users/42", formatterMulti.formatUrl("users/{id}"));
  }

  public void testMatrixParamsNameAndValueAreReplaced() {
    pathParams.put("res", "users");
    matrixParams.put("k", "role");
    matrixParams.put("v", "admin");
    assertEquals("/users;role=admin/42", formatterMulti.formatUrl("/{res};{k}={v}/42"));
  }

  public void testMissingMatrixParamThrowsAndMentionsContext() {
    pathParams.put("res", "users");
    try {
      formatterMulti.formatUrl("/{res};{k}={v}/42");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{k}"));
      assertTrue(e.getMessage().contains("matrix"));
    }
  }

  public void testMatrixMultiplePairsOnSameSegment() {
    pathParams.put("res", "m");
    matrixParams.put("k1", "x");
    matrixParams.put("v1", "1");
    matrixParams.put("k2", "y");
    matrixParams.put("v2", "2");
    assertEquals("/m;x=1;y=2", formatterMulti.formatUrl("/{res};{k1}={v1};{k2}={v2}"));
  }

  public void testQueryReplacesNamesAndValues() {
    queryParams.put("x", "page");
    queryParams.put("v", "2");
    assertEquals("/a?page=2&q=ok", formatterMulti.formatUrl("/a?{x}={v}&q=ok"));
  }

  public void testQueryMultipleSameKeysAfterReplacement() {
    queryParams.put("x", "page");
    queryParams.put("p1", "1");
    queryParams.put("p2", "2");
    assertEquals("/a?page=1&page=2", formatterMulti.formatUrl("/a?{x}={p1}&{x}={p2}"));
  }

  public void testMissingQueryParamThrowsAndMentionsContext() {
    try {
      formatterMulti.formatUrl("/a?{x}=2");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{x}"));
      assertTrue(e.getMessage().contains("query"));
    }
  }

  public void testFragmentReplacementAnywhereInFragment() {
    fragmentParams.put("frag", "alpha/beta");
    assertEquals("/a#pre/alpha/beta/post", formatterMulti.formatUrl("/a#pre/{frag}/post"));
  }

  public void testMissingFragmentParamThrowsAndMentionsContext() {
    try {
      formatterMulti.formatUrl("/a#{frag}");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{frag}"));
      assertTrue(e.getMessage().contains("fragment"));
    }
  }

  public void testMultipleHashesUseLastForFragment() {
    fragmentParams.put("k", "frag");
    assertEquals("/a?x=1#frag/child", formatterMulti.formatUrl("/a?x=1##{k}/child"));
  }

  public void testPathMultiplePlaceholdersMixedStyles() {
    pathParams.put("a", "users");
    pathParams.put("b", "42");
    assertEquals("/users/42", formatterMulti.formatUrl("/{a}/:b"));
  }

  public void testFullComboPathMatrixQueryFragment() {
    pathParams.put("res", "users");
    pathParams.put("id", "42");

    matrixParams.put("mk", "role");
    matrixParams.put("mv", "owner");

    queryParams.put("qk", "page");
    queryParams.put("qv", "2");

    fragmentParams.put("frag", "alpha/beta");

    String url = "/{res};{mk}={mv}/{id}?{qk}={qv}#{frag}";
    assertEquals("/users;role=owner/42?page=2#alpha/beta", formatterMulti.formatUrl(url));
  }

  public void testValuesWithReservedCharsAreInsertedVerbatim() {
    pathParams.put("id", "42");
    matrixParams.put("val", "x;y=1");
    queryParams.put("qv", "p=1&k=2");
    fragmentParams.put("fv", "alpha#beta");

    String url = "/users/{id};k={val}?q={qv}#{fv}";
    assertEquals("/users/42;k=x;y=1?q=p=1&k=2#alpha#beta", formatterMulti.formatUrl(url));
  }

  public void testMissingInMatrixButPresentElsewhereStillThrowsForMatrix() {
    queryParams.put("id", "77");
    try {
      formatterMulti.formatUrl("/a;{id}?id=ok");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{id}"));
      assertTrue(e.getMessage().contains("matrix"));
    }
  }

  public void testMissingInQueryButPresentInPathStillThrowsForQuery() {
    pathParams.put("x", "ok");
    try {
      formatterMulti.formatUrl("/{x}?{x}=1");
      fail("Expected PathParameterMissingException");
    } catch (PathParameterMissingException e) {
      assertTrue(e.getMessage().contains("{x}"));
      assertTrue(e.getMessage().contains("query"));
    }
  }

  public void testDuplicateSlashesNormalizedAndLastHashWins() {
    pathParams.put("a", "users");
    pathParams.put("b", "42");
    assertEquals("/users/42?x=1#frag", formatterMulti.formatUrl("///{a}//{b}///?x=1##frag"));
  }

  public void testSingleMapConstructorAppliesReplacementsToAllComponents() {
    pathParams.put("id", "42");
    pathParams.put("k", "role");
    pathParams.put("v", "admin");
    pathParams.put("x", "page");
    pathParams.put("p", "2");
    pathParams.put("frag", "alpha");

    String url = "/users/{id};{k}={v}?{x}={p}#{frag}";
    assertEquals("/users/42;role=admin?page=2#alpha", formatterSingle.formatUrl(url));
  }

  public void testMapsAreReferencedNotCopied() {
    Map<String, String> dynamicPath = new HashMap<>();
    UrlFormatter f =
        new UrlFormatter(
            dynamicPath, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    dynamicPath.put("x", "Y");
    assertEquals("/Y", f.formatUrl("/{x}"));
  }

  // ---------- {name:regex} support ----------

  public void testPathRegexSuccessFullMatchRequired() {
    pathParams.put("id", "12345");
    assertEquals("/users/12345", formatterMulti.formatUrl("/users/{id:\\d+}"));
  }

  public void testPathRegexFailureThrowsIllegalArgument() {
    pathParams.put("id", "abc"); // not digits
    try {
      formatterMulti.formatUrl("/users/{id:\\d+}");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("does not match"));
      assertTrue(e.getMessage().contains("path"));
    }
  }

  public void testMatrixRegexOnValue() {
    pathParams.put("res", "users");
    matrixParams.put("r", "eu-west-1");
    assertEquals(
        "/users;region=eu-west-1", formatterMulti.formatUrl("/{res};region={r:[a-z]+-[a-z]+-\\d}"));
  }

  public void testMatrixRegexOnValueFailure() {
    pathParams.put("res", "users");
    matrixParams.put("r", "EU");
    try {
      formatterMulti.formatUrl("/{res};region={r:[a-z]+-[a-z]+-\\d}");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("does not match"));
      assertTrue(e.getMessage().contains("matrix"));
    }
  }

  public void testQueryRegexOnNameAndValue() {
    queryParams.put("k", "page");
    queryParams.put("v", "12");
    assertEquals("/a?page=12", formatterMulti.formatUrl("/a?{k:[a-z]+}={v:\\d+}"));
  }

  public void testQueryRegexFailureOnName() {
    queryParams.put("k", "PAGE"); // uppercase should fail [a-z]+
    queryParams.put("v", "1");
    try {
      formatterMulti.formatUrl("/a?{k:[a-z]+}={v:\\d+}");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("does not match"));
      assertTrue(e.getMessage().contains("query"));
    }
  }

  public void testFragmentRegexSuccess() {
    fragmentParams.put("frag", "alpha-beta_1");
    assertEquals("/a#alpha-beta_1", formatterMulti.formatUrl("/a#{frag:[A-Za-z_-]+\\d}"));
  }

  public void testFragmentRegexFailure() {
    fragmentParams.put("frag", "alpha#beta");
    try {
      formatterMulti.formatUrl("/a#{frag:[A-Za-z_-]+\\d}");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("does not match"));
      assertTrue(e.getMessage().contains("fragment"));
    }
  }

  public void testSingleMapConstructorWorksWithRegex() {
    pathParams.put("id", "42");
    pathParams.put("k", "page");
    pathParams.put("v", "3");
    pathParams.put("frag", "ok_9");

    UrlFormatter single = new UrlFormatter(pathParams);
    assertEquals(
        "/users/42?page=3#ok_9",
        single.formatUrl("/users/{id:\\d+}?{k:[a-z]+}={v:\\d+}#{frag:ok_\\d}"));
  }

  public void testInvalidRegexInTemplateIsReportedClearly() {
    pathParams.put("id", "42");
    try {
      formatterMulti.formatUrl("/users/{id:(\\d+}");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Invalid regex"));
      assertTrue(e.getMessage().contains("path"));
    }
  }
}

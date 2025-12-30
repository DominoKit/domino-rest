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
import org.dominokit.rest.js.JsRegexEngine;
import org.dominokit.rest.shared.regex.RegexEngine;
import org.dominokit.rest.shared.request.UrlSplitUtil;

public class UrlSplitUtilGwtTest extends GWTTestCase {

  private RegexEngine engine;

  @Override
  public String getModuleName() {
    return "org.dominokit.rest.RestTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {

    engine = new JsRegexEngine();

    assertNotNull("RegexEngine must be initialized", engine);
  }

  private UrlSplitUtil newUtil() {
    assertNotNull("RegexEngine must be initialized", engine);
    return new UrlSplitUtil(engine);
  }

  private void assertSplit(String url, String expectedBase, String expectedRest) {
    UrlSplitUtil.Split split = newUtil().split(url);
    assertEquals("Base mismatch for: " + url, expectedBase, split.leftSide);
    assertEquals("Rest mismatch for: " + url, expectedRest, split.rightSide);
  }

  public void testSplit_basicAndTricky() {
    // No path / trailing slash / query / fragment
    assertSplit("http://example.com", "http://example.com", "");
    assertSplit("http://example.com/", "http://example.com", "/");
    assertSplit("http://example.com?x=1", "http://example.com", "?x=1");
    assertSplit("http://example.com#top", "http://example.com", "#top");

    // Port & path
    assertSplit("http://example.com:8080/foo", "http://example.com:8080", "/foo");
    assertSplit("http://example.com:8080/", "http://example.com:8080", "/");

    // HTTPS + userinfo (ignored in base)
    assertSplit("https://user:pass@sub.example.org/secret", "https://sub.example.org", "/secret");

    // Query/fragment only on authority
    assertSplit("https://sub.example.org?x=1&y=2", "https://sub.example.org", "?x=1&y=2");
    assertSplit("https://sub.example.org#hash", "https://sub.example.org", "#hash");

    // Protocol-relative
    assertSplit("//cdn.example.com/lib.js", "//cdn.example.com", "/lib.js");
    assertSplit("//cdn.example.com", "//cdn.example.com", "");

    // IPv6 (with and without port)
    assertSplit("https://[2001:db8::1]/a", "https://[2001:db8::1]", "/a");
    assertSplit("https://[2001:db8::1]:8443/a", "https://[2001:db8::1]:8443", "/a");

    // Relative / query / fragment only
    assertSplit("/relative/path", "", "/relative/path");
    assertSplit("relative/path", "", "relative/path");
    assertSplit("?q=1", "", "?q=1");
    assertSplit("#frag", "", "#frag");
    assertSplit("/", "", "/");

    // Other schemes with authority
    assertSplit("ftp://files.example.com/downloads", "ftp://files.example.com", "/downloads");

    // Unicode host/path
    assertSplit("https://mañana.example/olé", "https://mañana.example", "/olé");

    // Complex userinfo should be ignored from base
    assertSplit("http://user.name:pa:ss@exa-mple.com:99/p", "http://exa-mple.com:99", "/p");
  }

  public void testEmpty_string() {
    assertSplit("", "", "");
  }

  public void testMailto_no_authority() {
    assertSplit("mailto:someone@example.com", "", "mailto:someone@example.com");
  }

  public void testData_no_authority() {
    assertSplit("data:text/plain,hello", "", "data:text/plain,hello");
  }
}

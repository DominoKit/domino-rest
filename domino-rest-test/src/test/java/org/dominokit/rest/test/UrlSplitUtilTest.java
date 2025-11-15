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
package org.dominokit.rest.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;
import org.dominokit.rest.jvm.JvmRegexEngine;
import org.dominokit.rest.shared.regex.RegexEngine;
import org.dominokit.rest.shared.request.UrlSplitUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("UrlSplitUtil (JUnit 5)")
class UrlSplitUtilTest {

  private static RegexEngine engine;

  @BeforeAll
  static void initEngine() {
    // If you don't use SPI, replace this with:
    // engine = new YourConcreteRegexEngine();
    engine = new JvmRegexEngine();
  }

  private UrlSplitUtil newUtil() {
    assertNotNull(engine, "RegexEngine must be initialized");
    return new UrlSplitUtil(engine);
  }

  private void assertSplit(String url, String expectedBase, String expectedRest) {
    UrlSplitUtil.Split split = newUtil().split(url);
    assertEquals(expectedBase, split.leftSide, "Base mismatch for: " + url);
    assertEquals(expectedRest, split.rightSide, "Rest mismatch for: " + url);
  }

  // ---------------- Parameterized coverage ----------------

  static Stream<org.junit.jupiter.params.provider.Arguments> basicCases() {
    return Stream.of(
        // No path / trailing slash / query / fragment
        arg("http://example.com", "http://example.com", ""),
        arg("http://example.com/", "http://example.com", "/"),
        arg("http://example.com?x=1", "http://example.com", "?x=1"),
        arg("http://example.com#top", "http://example.com", "#top"),

        // Port & path
        arg("http://example.com:8080/foo", "http://example.com:8080", "/foo"),
        arg("http://example.com:8080/", "http://example.com:8080", "/"),

        // HTTPS + userinfo (ignored in base)
        arg("https://user:pass@sub.example.org/secret", "https://sub.example.org", "/secret"),

        // Query/fragment only on authority
        arg("https://sub.example.org?x=1&y=2", "https://sub.example.org", "?x=1&y=2"),
        arg("https://sub.example.org#hash", "https://sub.example.org", "#hash"),

        // Protocol-relative
        arg("//cdn.example.com/lib.js", "//cdn.example.com", "/lib.js"),
        arg("//cdn.example.com", "//cdn.example.com", ""),

        // IPv6 (with and without port)
        arg("https://[2001:db8::1]/a", "https://[2001:db8::1]", "/a"),
        arg("https://[2001:db8::1]:8443/a", "https://[2001:db8::1]:8443", "/a"),

        // Relative / query / fragment only
        arg("/relative/path", "", "/relative/path"),
        arg("relative/path", "", "relative/path"),
        arg("?q=1", "", "?q=1"),
        arg("#frag", "", "#frag"),
        arg("/", "", "/"),

        // Other schemes with authority
        arg("ftp://files.example.com/downloads", "ftp://files.example.com", "/downloads"),

        // Unicode host/path
        arg("https://mañana.example/olé", "https://mañana.example", "/olé"),

        // Complex userinfo should be ignored from base
        arg("http://user.name:pa:ss@exa-mple.com:99/p", "http://exa-mple.com:99", "/p"));
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("basicCases")
  @DisplayName("Split common and tricky absolute/relative URLs")
  void split_basicAndTricky(String url, String expectedBase, String expectedRest) {
    assertSplit(url, expectedBase, expectedRest);
  }

  // ---------------- Specific edge cases ----------------

  @Test
  @DisplayName("Empty string → base empty, rest empty")
  void empty_string() {
    assertSplit("", "", "");
  }

  @Test
  @DisplayName("mailto: treated as no-authority (falls back to rest)")
  void mailto_no_authority() {
    assertSplit("mailto:someone@example.com", "", "mailto:someone@example.com");
  }

  @Test
  @DisplayName("data: treated as no-authority (falls back to rest)")
  void data_no_authority() {
    assertSplit("data:text/plain,hello", "", "data:text/plain,hello");
  }

  // ---------------- Helpers ----------------

  private static org.junit.jupiter.params.provider.Arguments arg(
      String url, String base, String rest) {
    return org.junit.jupiter.params.provider.Arguments.of(url, base, rest);
  }
}

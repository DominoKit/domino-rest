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
package org.dominokit.rest.shared.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.dominokit.rest.shared.request.exception.PathParameterMissingException;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive tests for UrlFormatter covering: - Null/blank handling and no-op behavior without
 * expressions - Replacement in PATH segment names (pathParams) - Replacement in MATRIX params
 * (names & values, matrixParams) - Replacement in QUERY (names & values, queryParams) - Replacement
 * in FRAGMENT (fragmentParams) - Leading slash preservation, duplicate slashes, multiple '#' -
 * Missing-parameter failures per-context (path/matrix/query/fragment) - Mixed placeholders, mixed
 * styles, and round-trip normalization via ServicePath - Backward-compatibility constructor (single
 * map for all)
 */
public class UrlFormatterTests {

  private Map<String, String> pathParams;
  private Map<String, String> matrixParams;
  private Map<String, String> queryParams;
  private Map<String, String> fragmentParams;

  private UrlFormatter formatterMulti; // separate maps
  private UrlFormatter formatterSingle;

  static {
    DominoRestContext.make().init(new TestRestConfig());
  } // single-map backward-compat

  @Before
  public void setUp() {
    pathParams = new HashMap<>();
    matrixParams = new HashMap<>();
    queryParams = new HashMap<>();
    fragmentParams = new HashMap<>();

    formatterMulti = new UrlFormatter(pathParams, matrixParams, queryParams, fragmentParams);
    // For the single-map constructor, we’ll just reuse pathParams
    formatterSingle = new UrlFormatter(pathParams);
  }

  // ----- Basics: null/blank/no-expr -----

  @Test
  public void null_url_throws() {
    assertThatThrownBy(() -> formatterMulti.formatUrl(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void blank_url_returns_empty_string() {
    assertThat(formatterMulti.formatUrl("")).isEqualTo("");
    assertThat(formatterMulti.formatUrl("   ")).isEqualTo("");
  }

  @Test
  public void url_without_expressions_is_returned_as_is() {
    assertThat(formatterMulti.formatUrl("/movies/hulk")).isEqualTo("/movies/hulk");
    // Unbalanced braces don't count as expressions:
    assertThat(formatterMulti.formatUrl("/movies/{hulk{")).isEqualTo("/movies/{hulk{");
    assertThat(formatterMulti.formatUrl("/movies/}hulk}")).isEqualTo("/movies/}hulk}");
  }

  // ----- PATH expressions -----

  @Test
  public void path_placeholders_both_styles_replaced() {
    pathParams.put("name", "hulk");
    assertThat(formatterMulti.formatUrl("/movies/{name}")).isEqualTo("/movies/hulk");
    assertThat(formatterMulti.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
  }

  @Test
  public void missing_path_param_throws_and_mentions_token_and_context() {
    assertThatThrownBy(() -> formatterMulti.formatUrl("/movies/{name}"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{name}")
        .hasMessageContaining("path");
  }

  @Test
  public void leading_slash_preserved_no_double_slash() {
    pathParams.put("id", "42");
    assertThat(formatterMulti.formatUrl("/users/{id}")).isEqualTo("/users/42");
    assertThat(formatterMulti.formatUrl("users/{id}")).isEqualTo("users/42");
  }

  // ----- MATRIX expressions -----

  @Test
  public void matrix_params_name_and_value_are_replaced() {
    pathParams.put("res", "users");
    matrixParams.put("k", "role");
    matrixParams.put("v", "admin");
    assertThat(formatterMulti.formatUrl("/{res};{k}={v}/42")).isEqualTo("/users;role=admin/42");
  }

  @Test
  public void missing_matrix_param_throws_and_mentions_context() {
    pathParams.put("res", "users");
    assertThatThrownBy(() -> formatterMulti.formatUrl("/{res};{k}={v}/42"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{k}")
        .hasMessageContaining("matrix");
  }

  @Test
  public void matrix_multiple_pairs_on_same_segment() {
    pathParams.put("res", "m");
    matrixParams.put("k1", "x");
    matrixParams.put("v1", "1");
    matrixParams.put("k2", "y");
    matrixParams.put("v2", "2");
    assertThat(formatterMulti.formatUrl("/{res};{k1}={v1};{k2}={v2}")).isEqualTo("/m;x=1;y=2");
  }

  // ----- QUERY expressions -----

  @Test
  public void query_replaces_names_and_values() {
    queryParams.put("x", "page");
    queryParams.put("v", "2");
    assertThat(formatterMulti.formatUrl("/a?{x}={v}&q=ok")).isEqualTo("/a?page=2&q=ok");
  }

  @Test
  public void query_multiple_same_keys_after_replacement() {
    queryParams.put("x", "page");
    queryParams.put("p1", "1");
    queryParams.put("p2", "2");
    assertThat(formatterMulti.formatUrl("/a?{x}={p1}&{x}={p2}")).isEqualTo("/a?page=1&page=2");
  }

  @Test
  public void missing_query_param_throws_and_mentions_context() {
    assertThatThrownBy(() -> formatterMulti.formatUrl("/a?{x}=2"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{x}")
        .hasMessageContaining("query");
  }

  // ----- FRAGMENT expressions -----

  @Test
  public void fragment_replacement_anywhere_in_fragment() {
    fragmentParams.put("frag", "alpha/beta");
    assertThat(formatterMulti.formatUrl("/a#pre/{frag}/post")).isEqualTo("/a#pre/alpha/beta/post");
  }

  @Test
  public void missing_fragment_param_throws_and_mentions_context() {
    assertThatThrownBy(() -> formatterMulti.formatUrl("/a#{frag}"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{frag}")
        .hasMessageContaining("fragment");
  }

  @Test
  public void multiple_hashes_use_last_for_fragment() {
    fragmentParams.put("k", "frag");
    assertThat(formatterMulti.formatUrl("/a?x=1##{k}/child")).isEqualTo("/a?x=1#frag/child");
  }

  // ----- Mixed styles and contexts -----

  @Test
  public void path_multiple_placeholders_mixed_styles() {
    pathParams.put("a", "users");
    pathParams.put("b", "42");
    assertThat(formatterMulti.formatUrl("/{a}/:b")).isEqualTo("/users/42");
  }

  @Test
  public void full_combo_path_matrix_query_fragment() {
    pathParams.put("res", "users");
    pathParams.put("id", "42");

    matrixParams.put("mk", "role");
    matrixParams.put("mv", "owner");

    queryParams.put("qk", "page");
    queryParams.put("qv", "2");

    fragmentParams.put("frag", "alpha/beta");

    String url = "/{res};{mk}={mv}/{id}?{qk}={qv}#{frag}";
    assertThat(formatterMulti.formatUrl(url)).isEqualTo("/users;role=owner/42?page=2#alpha/beta");
  }

  // ----- Reserved characters are inserted verbatim (no encoding) -----

  @Test
  public void values_with_reserved_chars_are_inserted_verbatim() {
    pathParams.put("id", "42");
    matrixParams.put("val", "x;y=1"); // semicolon included
    queryParams.put("qv", "p=1&k=2"); // ampersand splits queries, left as-is
    fragmentParams.put("fv", "alpha#beta"); // everything after last '#' is fragment anyway

    String url = "/users/{id};k={val}?q={qv}#{fv}";
    assertThat(formatterMulti.formatUrl(url)).isEqualTo("/users/42;k=x;y=1?q=p=1&k=2#alpha#beta");
  }

  // ----- Context-specific missing keys (ensure we validate against the correct map) -----

  @Test
  public void missing_in_matrix_but_present_elsewhere_still_throws_for_matrix() {
    queryParams.put("id", "77");
    assertThatThrownBy(() -> formatterMulti.formatUrl("/a;{id}?id=ok"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{id}")
        .hasMessageContaining("matrix");
  }

  @Test
  public void missing_in_query_but_present_in_path_still_throws_for_query() {
    pathParams.put("x", "ok");
    assertThatThrownBy(() -> formatterMulti.formatUrl("/{x}?{x}=1"))
        .isInstanceOf(PathParameterMissingException.class)
        .hasMessageContaining("{x}")
        .hasMessageContaining("query");
  }

  // ----- Duplicate slashes and normalization via ServicePath -----

  @Test
  public void duplicate_slashes_normalized_and_last_hash_wins() {
    pathParams.put("a", "users");
    pathParams.put("b", "42");
    assertThat(formatterMulti.formatUrl("///{a}//{b}///?x=1##frag"))
        .isEqualTo("/users/42?x=1#frag");
  }

  // ----- Backward-compat constructor (single map for all components) -----

  @Test
  public void single_map_constructor_applies_replacements_to_all_components() {
    pathParams.put("id", "42");
    pathParams.put("k", "role");
    pathParams.put("v", "admin");
    pathParams.put("x", "page");
    pathParams.put("p", "2");
    pathParams.put("frag", "alpha");

    String url = "/users/{id};{k}={v}?{x}={p}#{frag}";
    assertThat(formatterSingle.formatUrl(url)).isEqualTo("/users/42;role=admin?page=2#alpha");
  }

  // ----- Maps are referenced (not copied) -----

  @Test
  public void maps_are_referenced_not_copied() {
    Map<String, String> dynamicPath = new HashMap<>();
    UrlFormatter f =
        new UrlFormatter(
            dynamicPath, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    dynamicPath.put("x", "Y");
    assertThat(f.formatUrl("/{x}")).isEqualTo("/Y");
  }

  // ---------- {name:regex} support ----------

  @Test
  public void path_regex_success_full_match_required() {
    pathParams.put("id", "12345");
    assertThat(formatterMulti.formatUrl("/users/{id:\\d+}")).isEqualTo("/users/12345");
  }

  @Test
  public void path_regex_failure_throws_illegalArgument() {
    pathParams.put("id", "abc"); // not digits
    assertThatThrownBy(() -> formatterMulti.formatUrl("/users/{id:\\d+}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not match")
        .hasMessageContaining("path");
  }

  @Test
  public void matrix_regex_on_value() {
    pathParams.put("res", "users");
    matrixParams.put("r", "eu-west-1");
    assertThat(formatterMulti.formatUrl("/{res};region={r:[a-z]+-[a-z]+-\\d}"))
        .isEqualTo("/users;region=eu-west-1");
  }

  @Test
  public void matrix_regex_on_value_failure() {
    pathParams.put("res", "users");
    matrixParams.put("r", "EU"); // not matching pattern
    assertThatThrownBy(() -> formatterMulti.formatUrl("/{res};region={r:[a-z]+-[a-z]+-\\d}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not match")
        .hasMessageContaining("matrix");
  }

  @Test
  public void query_regex_on_name_and_value() {
    queryParams.put("k", "page");
    queryParams.put("v", "12");
    assertThat(formatterMulti.formatUrl("/a?{k:[a-z]+}={v:\\d+}")).isEqualTo("/a?page=12");
  }

  @Test
  public void query_regex_failure_on_name() {
    queryParams.put("k", "PAGE"); // uppercase should fail [a-z]+
    queryParams.put("v", "1");
    assertThatThrownBy(() -> formatterMulti.formatUrl("/a?{k:[a-z]+}={v:\\d+}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not match")
        .hasMessageContaining("query");
  }

  @Test
  public void fragment_regex_success() {
    fragmentParams.put("frag", "alpha-beta_1");
    assertThat(formatterMulti.formatUrl("/a#{frag:[A-Za-z_-]+\\d}")).isEqualTo("/a#alpha-beta_1");
  }

  @Test
  public void fragment_regex_failure() {
    fragmentParams.put("frag", "alpha#beta"); // '#' not in class
    assertThatThrownBy(() -> formatterMulti.formatUrl("/a#{frag:[A-Za-z_-]+\\d}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not match")
        .hasMessageContaining("fragment");
  }

  @Test
  public void single_map_constructor_works_with_regex() {
    // Using the back-compat single map for all parts:
    pathParams.put("id", "42");
    pathParams.put("k", "page");
    pathParams.put("v", "3");
    pathParams.put("frag", "ok_9");

    UrlFormatter single = new UrlFormatter(pathParams);
    assertThat(single.formatUrl("/users/{id:\\d+}?{k:[a-z]+}={v:\\d+}#{frag:ok_\\d}"))
        .isEqualTo("/users/42?page=3#ok_9");
  }

  @Test
  public void invalid_regex_in_template_is_reported_clearly() {
    pathParams.put("id", "42");
    // Unclosed group '('
    assertThatThrownBy(() -> formatterMulti.formatUrl("/users/{id:(\\d+}"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid regex")
        .hasMessageContaining("path");
  }
}

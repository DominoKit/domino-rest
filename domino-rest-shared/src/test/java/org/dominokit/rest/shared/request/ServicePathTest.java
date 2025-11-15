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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 * Comprehensive tests for {@link ServicePath} covering: - Basic path/fragment/query parsing -
 * Matrix parameter parsing & rendering per path segment - Multiple values per key (query & matrix)
 * - Empty values, repeated keys, ordering - Root rebasing and full value/noRootValue rendering -
 * endsWithPath semantics (ignores matrix) - Query & matrix set/append/remove mutations -
 * equals/hashCode and round-trip fidelity - Edge cases: ?, #, ;, !, missing '=', empty path
 * segments, leading/trailing slashes - Current replacePath behavior (documented)
 */
public class ServicePathTest {

  private static ServicePath sp(String token) {
    return new ServicePath(token);
  }

  private static ServicePath sp(String root, String token) {
    return new ServicePath(root, token);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor_nullToken_throws() {
    new ServicePath(null);
  }

  @Test
  public void constructor_emptyToken_ok_andEmpty() {
    ServicePath s = sp("");
    assertTrue(s.paths().isEmpty());
    assertTrue(s.fragments().isEmpty());
    assertEquals("", s.query());
    assertEquals("", s.path());
    assertEquals("", s.fragment());
    assertTrue(s.isEmpty());
  }

  @Test
  public void basic_paths_query_fragment() {
    ServicePath s = sp("/a/b/c?x=1&y=2#d/e");
    assertEquals(Arrays.asList("a", "b", "c"), s.paths());
    assertEquals("x=1&y=2", s.query());
    assertEquals(Arrays.asList("d", "e"), s.fragments());
    // path() includes matrix if any; here none:
    assertEquals("a/b/c", s.path());
    assertEquals("d/e", s.fragment());
    assertFalse(s.isEmpty());
  }

  @Test
  public void matrix_singleSegment_singleParam() {
    ServicePath s = sp("/users;active=true");
    assertEquals(Arrays.asList("users"), s.paths());
    assertEquals("users;active=true", s.path());
    assertTrue(s.hasMatrixParameter(0, "active"));
    assertEquals(Collections.singletonList("true"), s.matrixParameterValues(0, "active"));
  }

  @Test
  public void matrix_multipleSegments_multipleParams() {
    ServicePath s = sp("/users;region=eu;active=true/42;tags=a;tags=b");
    assertEquals(Arrays.asList("users", "42"), s.paths());
    assertEquals("users;region=eu;active=true/42;tags=a;tags=b", s.path());

    assertTrue(s.hasMatrixParameter(0, "region"));
    assertTrue(s.hasMatrixParameter(0, "active"));
    assertEquals(Collections.singletonList("eu"), s.matrixParameterValues(0, "region"));
    assertEquals(Collections.singletonList("true"), s.matrixParameterValues(0, "active"));

    assertTrue(s.hasMatrixParameter(1, "tags"));
    assertEquals(Arrays.asList("a", "b"), s.matrixParameterValues(1, "tags"));
  }

  @Test
  public void matrix_emptyValueIsPreserved() {
    ServicePath s = sp("/x;empty=/y;kv=;kv=v2");
    assertEquals(Arrays.asList("x", "y"), s.paths());
    assertEquals(Collections.singletonList(""), s.matrixParameterValues(1, "kv").subList(0, 1));
    assertEquals(Arrays.asList("", "v2"), s.matrixParameterValues(1, "kv"));
    assertEquals("x;empty=/y;kv=;kv=v2", s.path());
  }

  @Test
  public void matrix_firstSegmentWithoutName_allowed() {
    // Segment starts with ';' so name is empty string, matrix exists.
    ServicePath s = sp(";a=1/b");
    assertEquals(Arrays.asList("", "b"), s.paths()); // name cache contains "" for first
    assertTrue(s.hasMatrixParameter(0, "a"));
    assertEquals(Collections.singletonList("1"), s.matrixParameterValues(0, "a"));
    assertEquals(";a=1/b", s.path());
  }

  @Test
  public void matrix_orderOfValuesIsPreserved() {
    ServicePath s = sp("/m;v=1;v=3;v=2");
    assertEquals(Arrays.asList("1", "3", "2"), s.matrixParameterValues(0, "v"));
    assertEquals("m;v=1;v=3;v=2", s.path());
  }

  @Test
  public void endsWithPath_ignoresMatrix() {
    ServicePath s = sp("/a;x=1/b;y=2/c");
    assertTrue(s.endsWithPath("b/c"));
    assertTrue(s.endsWithPath("a/b/c"));
    assertFalse(s.endsWithPath("a/c"));
    assertTrue(s.endsWithPath("c"));
  }

  @Test
  public void query_set_append_remove_behaviour() {
    ServicePath s = sp("/a?x=1&y=2&y=3");
    assertTrue(s.hasQueryParameter("x"));
    assertTrue(s.hasQueryParameter("y"));
    assertEquals("x=1&y=2&y=3", s.query());

    // set replaces
    s.setQueryParameter("x", "9");
    assertEquals("x=9&y=2&y=3", s.query());

    // append adds
    s.appendParameter("y", "4");
    assertEquals("x=9&y=2&y=3&y=4", s.query());

    // set with list replaces all y
    s.setQueryParameter("y", Arrays.asList("a", "b"));
    assertEquals("x=9&y=a&y=b", s.query());

    // remove
    s.removeParameter("x");
    assertEquals("y=a&y=b", s.query());
  }

  @Test(expected = IllegalArgumentException.class)
  public void query_missingEquals_throws() {
    sp("/a?bad#frag"); // 'bad' lacks '='
  }

  @Test
  public void query_emptyValue_isKept() {
    ServicePath s = sp("/a?e=&k=v");
    assertEquals("e=&k=v", s.query());
  }

  @Test
  public void matrix_set_append_remove_behaviour() {
    ServicePath s = sp("/p;q=1;p2;z=9/r");
    // Initial checks
    assertTrue(s.hasMatrixParameter(0, "q"));
    assertTrue(s.hasMatrixParameter(0, "z"));
    assertFalse(s.hasMatrixParameter(1, "q"));

    // set replaces (single)
    s.setMatrixParameter(0, "q", "7");
    assertEquals(Collections.singletonList("7"), s.matrixParameterValues(0, "q"));

    // append adds additional values for same key
    s.appendMatrixParameter(0, "q", "8");
    assertEquals(Arrays.asList("7", "8"), s.matrixParameterValues(0, "q"));

    // set with list replaces
    s.setMatrixParameter(0, "z", Arrays.asList("x", "y"));
    assertEquals(Arrays.asList("x", "y"), s.matrixParameterValues(0, "z"));

    // remove parameter
    s.removeMatrixParameter(0, "z");
    assertFalse(s.hasMatrixParameter(0, "z"));

    // Rendering reflects changes
    assertEquals("p;q=7;q=8;p2/r", s.path());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void matrix_invalidSegment_throws() {
    ServicePath s = sp("/a");
    s.setMatrixParameter(5, "k", "v");
  }

  @Test
  public void value_withRoot_noExtraSlashWhenAppropriate() {
    ServicePath s = sp("/api", "/api/users;active=true/42?x=1#f");
    assertEquals("users;active=true/42", s.path());
    assertEquals("/api/users;active=true/42?x=1#f", s.value());
    assertEquals("/users;active=true/42?x=1#f", s.noRootValue());
  }

  @Test
  public void value_withRoot_noDoubleSlash() {
    ServicePath s = sp("/api/", "/api/users");
    assertEquals("/api/users", s.value());
  }

  @Test
  public void value_withoutRoot_andAbsoluteToken_keepsLeadingSlashOutOfInternalState() {
    ServicePath s = sp("/a/b;c=1?x=1#f");
    assertEquals("a/b;c=1", s.path());
  }

  @Test
  public void value_roundTrip_fidelity() {
    String token = "/users;region=eu;active=true/42;tag=a;tag=b?x=1&y=2#alpha/beta";
    ServicePath s = sp(token);
    String rendered = s.value(); // no root: should equal original token (same semantics)
    assertEquals(token, rendered);
    ServicePath s2 = sp(rendered);
    assertEquals(s, s2);
    assertEquals(s.hashCode(), s2.hashCode());
  }

  @Test
  public void endsWithPath_various() {
    ServicePath s = sp("/a/b;c=1/d;e=2");
    assertTrue(s.endsWithPath("d"));
    assertTrue(s.endsWithPath("b/d"));
    assertTrue(s.endsWithPath("a/b/d"));
    assertFalse(s.endsWithPath("a/d"));
    assertFalse(s.endsWithPath(""));
  }

  @Test
  public void fragments_parsingAndRendering() {
    ServicePath s = sp("/a#b/c/d");
    assertEquals(Arrays.asList("b", "c", "d"), s.fragments());
    assertEquals("b/c/d", s.fragment());
    assertEquals("/a#b/c/d", s.noRootValue());
  }

  @Test
  public void fragments_emptyOrMissing() {
    ServicePath s1 = sp("/a#");
    assertTrue(s1.fragments().isEmpty());
    assertEquals("a", s1.path());

    ServicePath s2 = sp("/a");
    assertTrue(s2.fragments().isEmpty());
    assertEquals("a", s2.path());
  }

  @Test
  public void exclamation_marks_areStrippedFromPathPart() {
    ServicePath s = sp("/!a/!!b;c=1?x=1#f");
    assertEquals(Arrays.asList("a", "b"), s.paths());
    assertEquals("a/b;c=1", s.path()); // matrix stays
  }

  @Test
  public void fragments_multiple_hashes_take_last() {
    ServicePath s = sp("/a?x=1##frag/child");
    assertEquals(Arrays.asList("frag", "child"), s.fragments());
    assertEquals("frag/child", s.fragment());
  }

  @Test
  public void leading_trailing_and_duplicate_slashes() {
    ServicePath s = sp("///a//b;c=1///?x=1##frag/child");
    assertEquals(Arrays.asList("a", "b"), s.paths());
    assertEquals("a/b;c=1", s.path());
    assertEquals("x=1", s.query());
    assertEquals(Arrays.asList("frag", "child"), s.fragments());
  }

  @Test
  public void equals_ignores_order_of_query_values_and_matrix_values_but_considers_multiplicity() {
    ServicePath s1 = sp("/a;b=1;b=2/c;d=3?x=1&x=2#f");
    ServicePath s2 = sp("/a;b=2;b=1/c;d=3?x=2&x=1#f");
    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());

    ServicePath s3 = sp("/a;b=1/c;d=3?x=1#f");
    assertNotEquals(s1, s3);
  }

  @Test
  public void replacePath_updatesSegmentName_andRendering_nowCorrect() {
    ServicePath s = new ServicePath("/a;x=1/b;y=2/c");
    assertEquals(Arrays.asList("a", "b", "c"), s.paths());
    s.replacePath("b", "B");

    // paths() cache updated
    assertEquals(Arrays.asList("a", "B", "c"), s.paths());

    // path() now reflects the renamed segment while preserving matrix params
    assertEquals("a;x=1/B;y=2/c", s.path());
  }

  @Test
  public void pathWithoutMatrix_returns_names_only() {
    ServicePath s = sp("/x;a=1;b=2/y;c=3");
    assertEquals("x/y", s.pathWithoutMatrix());
    assertEquals("x;a=1;b=2/y;c=3", s.path());
  }

  @Test
  public void noRootValue_and_value_composition_cases() {
    ServicePath s1 = sp("/root", "/root/a;b=1?x=1#f");
    assertEquals("/a;b=1?x=1#f", s1.noRootValue());
    assertEquals("/root/a;b=1?x=1#f", s1.value());

    ServicePath s2 = sp("/root/", "/root/a");
    assertEquals("/root/a", s2.value());

    ServicePath s3 = sp("", "a;b=1/b;c=2?x=1#f/g");
    assertEquals("a;b=1/b;c=2?x=1#f/g", s3.value());
    assertEquals("a;b=1/b;c=2?x=1#f/g", s3.noRootValue());
  }

  @Test
  public void isEmpty_onlyWhen_allPartsEmpty() {
    assertTrue(sp("").isEmpty());
    assertFalse(sp("/a").isEmpty());
    assertFalse(sp("?x=1").isEmpty());
    assertFalse(sp("#f").isEmpty());
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void matrixValues_invalidIndex_throws() {
    sp("/a").matrixParameterValues(1, "k");
  }

  @Test
  public void complex_combined_roundTrip_and_mutations() {
    ServicePath s = sp("/api", "/api/users;role=admin;role=owner/42;active=true?x=1&x=2#sec/a");
    // Initial checks
    assertEquals(Arrays.asList("users", "42"), s.paths());
    assertEquals("users;role=admin;role=owner/42;active=true", s.path());
    assertEquals("x=1&x=2", s.query());
    assertEquals(Arrays.asList("sec", "a"), s.fragments());
    assertEquals("/api/users;role=admin;role=owner/42;active=true?x=1&x=2#sec/a", s.value());

    // Mutations
    s.setMatrixParameter(1, "active", Arrays.asList("true", "yes")); // 42;active=true;active=yes
    s.appendMatrixParameter(0, "role", "auditor"); // users;role=...;role=auditor
    s.setQueryParameter("x", Arrays.asList("10", "20")); // x=10&x=20
    s.removeMatrixParameter(0, "nonexistent"); // no-op

    assertEquals(Arrays.asList("admin", "owner", "auditor"), s.matrixParameterValues(0, "role"));
    assertEquals(Arrays.asList("true", "yes"), s.matrixParameterValues(1, "active"));
    assertEquals("x=10&x=20", s.query());

    // Round trip again (preserve the same root so the structure matches)
    ServicePath s2 = sp("/api", s.value());
    assertEquals(s, s2);
    assertEquals(s.hashCode(), s2.hashCode());
  }

  @Test
  public void fragments_preserve_internal_hash_characters() {
    // Fragment contains an internal '#': everything after the FIRST '#' belongs to the fragment.
    String token = "/a#alpha#beta";
    ServicePath s = new ServicePath(token);

    // fragments() splits only on '/', not on '#'
    assertEquals(java.util.Arrays.asList("alpha#beta"), s.fragments());
    assertEquals("alpha#beta", s.fragment());

    // Round-trip should preserve the internal '#'
    assertEquals(token, s.value());
  }

  @Test
  public void fragments_double_hash_after_delimiter_strips_extra_leading_hashes() {
    // Two hashes in a row -> fragment should start after the extra '#'
    ServicePath s = new ServicePath("/a?x=1##frag/child");

    // We normalize "##frag/child" -> "frag/child"
    assertEquals(java.util.Arrays.asList("frag", "child"), s.fragments());
    assertEquals("frag/child", s.fragment());
    assertEquals("/a?x=1#frag/child", s.value());
  }
}

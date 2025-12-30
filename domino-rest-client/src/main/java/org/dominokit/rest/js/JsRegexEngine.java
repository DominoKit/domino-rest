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
package org.dominokit.rest.js;

import org.dominokit.rest.shared.regex.DominoCompiledPattern;
import org.dominokit.rest.shared.regex.DominoMatcher;
import org.dominokit.rest.shared.regex.RegexEngine;
import org.dominokit.rest.shared.regex.Replacer;
import org.gwtproject.regexp.shared.MatchResult;
import org.gwtproject.regexp.shared.RegExp;

public final class JsRegexEngine implements RegexEngine {

  @Override
  public DominoCompiledPattern compile(String pattern) {
    return new GwtCompiledPattern(RegExp.compile(pattern), "");
  }

  @Override
  public DominoCompiledPattern compile(String pattern, String flags) {
    String f = flags == null ? "" : flags;
    // GWT supports "g", "i", "m". (No "s"/dotall in GWT.)
    return new GwtCompiledPattern(RegExp.compile(pattern, f), f);
  }

  @Override
  public String replaceAll(String input, DominoCompiledPattern compiled, Replacer replacer) {
    GwtCompiledPattern cp = (GwtCompiledPattern) compiled;

    // Ensure global iteration. If the compiled pattern isn’t global, recreate with 'g' added.
    String flags = cp.flags.contains("g") ? cp.flags : (cp.flags + "g");
    RegExp re = flags.equals(cp.flags) ? cp.re : RegExp.compile(cp.re.getSource(), flags);

    StringBuilder out = new StringBuilder(input.length() + 16);
    int lastIndex = 0;

    while (true) {
      // exec() advances according to 'lastIndex' when 'g' is set.
      MatchResult mr = re.exec(input);
      if (mr == null) break;

      int matchStart = mr.getIndex();
      String whole = mr.getGroup(0);
      int matchEnd = matchStart + (whole == null ? 0 : whole.length());

      if (matchStart > lastIndex) {
        out.append(input, lastIndex, matchStart);
      }

      String replacement = replacer.replace(new GwtMatcherSnapshot(mr));
      out.append(replacement);

      // Guard against zero-length matches (e.g., patterns like "^" or "$" or lookaheads).
      if (matchEnd == lastIndex) {
        matchEnd = Math.min(input.length(), lastIndex + 1);
      }

      lastIndex = matchEnd;
      re.setLastIndex(lastIndex);
    }

    if (lastIndex < input.length()) {
      out.append(input.substring(lastIndex));
    }
    return out.toString();
  }

  @Override
  public boolean matches(String pattern, String candidate) {
    // full match by anchoring
    RegExp re = RegExp.compile("^(?:" + pattern + ")$");
    return re.test(candidate);
  }

  @Override
  public boolean matches(String pattern, String candidate, String flags) {
    String f = flags == null ? "" : flags.replace("g", ""); // 'g' irrelevant for full match
    RegExp re = RegExp.compile("^(?:" + pattern + ")$", f);
    return re.test(candidate);
  }

  // ---- wrappers -----------------------------------------------------------

  private static final class GwtCompiledPattern implements DominoCompiledPattern {
    final RegExp re;
    final String flags;

    GwtCompiledPattern(RegExp re, String flags) {
      this.re = re;
      this.flags = flags == null ? "" : flags;
    }

    @Override
    public DominoMatcher matcher(String input) {
      return new GwtMatcherSnapshot(re.exec(input));
    }

    @Override
    public String pattern() {
      return re.getSource();
    }

    @Override
    public String flags() {
      return flags;
    }
  }

  /**
   * Snapshot wrapper over a single {@link MatchResult}. This mirrors the JVM matcher interface for
   * the replacer callback.
   */
  private static final class GwtMatcherSnapshot implements DominoMatcher {
    final MatchResult r;

    GwtMatcherSnapshot(MatchResult r) {
      this.r = r;
    }

    @Override
    public boolean find() {
      throw new UnsupportedOperationException("Use RegexEngine.replaceAll for iteration.");
    }

    @Override
    public boolean isMatch() {
      return r != null;
    }

    @Override
    public String group(int index) {
      return r.getGroup(index);
    }

    @Override
    public int groupCount() {
      return r.getGroupCount();
    } // includes group 0

    @Override
    public int start() {
      return r.getIndex();
    }

    @Override
    public int end() {
      String g0 = r.getGroup(0);
      return r.getIndex() + (g0 == null ? 0 : g0.length());
    }

    @Override
    public void reset() {
      /* snapshot; no-op */
    }
  }
}

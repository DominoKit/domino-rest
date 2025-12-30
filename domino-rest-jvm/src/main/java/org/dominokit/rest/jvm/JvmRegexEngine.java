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
package org.dominokit.rest.jvm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dominokit.rest.shared.regex.DominoCompiledPattern;
import org.dominokit.rest.shared.regex.DominoMatcher;
import org.dominokit.rest.shared.regex.RegexEngine;
import org.dominokit.rest.shared.regex.Replacer;

/** JVM implementation of the {@link RegexEngine}. */
public class JvmRegexEngine implements RegexEngine {

  @Override
  public DominoCompiledPattern compile(String pattern) {
    return new JvmDominoCompiledPattern(Pattern.compile(pattern), "");
  }

  @Override
  public DominoCompiledPattern compile(String pattern, String flags) {
    int f = 0;
    if (flags != null) {
      if (flags.indexOf('i') >= 0) f |= Pattern.CASE_INSENSITIVE;
      if (flags.indexOf('m') >= 0) f |= Pattern.MULTILINE;
      if (flags.indexOf('s') >= 0) f |= Pattern.DOTALL;
      // 'g' has no direct meaning for JVM; replaceAll controls iteration.
    }
    return new JvmDominoCompiledPattern(Pattern.compile(pattern, f), flags == null ? "" : flags);
  }

  @Override
  public String replaceAll(String input, DominoCompiledPattern p, Replacer replacer) {
    JvmDominoCompiledPattern cp = (JvmDominoCompiledPattern) p;
    Matcher m = cp.pattern.matcher(input);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String replacement = replacer.replace(new JvmMatcher(m));
      m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  @Override
  public boolean matches(String pattern, String candidate) {
    return Pattern.compile(pattern).matcher(candidate).matches();
  }

  @Override
  public boolean matches(String pattern, String candidate, String flags) {
    return ((JvmDominoCompiledPattern) compile("^(?:" + pattern + ")$", flags))
        .pattern
        .matcher(candidate)
        .matches();
  }

  // --- wrappers ---

  static final class JvmDominoCompiledPattern implements DominoCompiledPattern {
    final Pattern pattern;
    final String flags;

    JvmDominoCompiledPattern(Pattern pattern, String flags) {
      this.pattern = pattern;
      this.flags = flags;
    }

    @Override
    public DominoMatcher matcher(String input) {
      return new JvmMatcher(pattern.matcher(input));
    }

    @Override
    public String pattern() {
      return pattern.pattern();
    }

    @Override
    public String flags() {
      return flags;
    }
  }

  static final class JvmMatcher implements DominoMatcher {
    final Matcher m;

    JvmMatcher(Matcher m) {
      this.m = m;
    }

    @Override
    public boolean find() {
      return m.find();
    }

    @Override
    public boolean isMatch() {
      return find();
    }

    @Override
    public String group(int index) {
      return m.group(index);
    }

    @Override
    public int groupCount() {
      return m.groupCount();
    }

    @Override
    public int start() {
      return m.start();
    }

    @Override
    public int end() {
      return m.end();
    }

    @Override
    public void reset() {
      m.reset();
    }
  }
}

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
package org.dominokit.rest.shared.regex;

/** Minimal matcher API we need across JVM/Browser. */
public interface RegexEngine {
  DominoCompiledPattern compile(String pattern); // no flags

  DominoCompiledPattern compile(String pattern, String flags); // e.g. "gim" (JS-style flags)

  /**
   * Replace all matches in {@code input} using {@code pattern}, producing the replacement via the
   * callback. The callback receives a snapshot of the current match. Implementations MUST be safe
   * for patterns that can match empty strings (advance at least 1 char).
   *
   * @param input the input string
   * @param pattern the compiled pattern
   * @param replacer the replacer callback
   * @return the resulting string after replacements
   */
  String replaceAll(String input, DominoCompiledPattern pattern, Replacer replacer);

  /**
   * Convenience full-match check (equivalent to ^(?:pattern)$).
   *
   * @param pattern the regular expression pattern
   * @param candidate the string to check
   * @return true if the candidate matches the pattern, false otherwise
   */
  boolean matches(String pattern, String candidate);

  /**
   * Same as matches, but allow explicit flags; implementations may ignore unsupported flags.
   *
   * @param pattern the regular expression pattern
   * @param candidate the string to check
   * @param flags the regular expression flags
   * @return true if the candidate matches the pattern, false otherwise
   */
  boolean matches(String pattern, String candidate, String flags);
}

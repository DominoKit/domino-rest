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

/** Interface for a compiled regular expression pattern. */
public interface DominoCompiledPattern {
  /**
   * Creates a matcher for the given input.
   *
   * @param input the input string to match against
   * @return a {@link DominoMatcher}
   */
  DominoMatcher matcher(String input);

  /**
   * @return the original pattern text
   */
  String pattern(); // original pattern text

  /**
   * @return the flag string like "gim" (empty if none)
   */
  String flags(); // flag string like "gim" (empty if none)
}

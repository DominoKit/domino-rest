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

public interface DominoMatcher {
  /** Find next match from current position. */
  boolean find();

  boolean isMatch();

  /** Group value 0..groupCount(). */
  String group(int index);

  /** Number of groups including the entire match at index 0. */
  int groupCount();

  int start(); // start index of current match

  int end(); // end index (exclusive)

  void reset(); // reset to search again from beginning
}

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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickTest {
  // NEW PATTERN
  private static final String EXPR_PATTERN =
      "(?::([A-Za-z0-9_.-]+))" // :name -> group 1
          + "|\\{([A-Za-z0-9_.-]+)" // {name
          + "(?::((?:[^}]|}(?=}))*))?" //   :regex (may contain } if followed by })
          + "\\}"; // }

  public static void main(String[] args) {
    Arrays.asList(
            "{accountNum:[0-9]{8,12}}",
            "{orderId: [a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}}")
        .forEach(
            template -> {
              System.out.println();
              System.out.println();

              System.out.println("Pattern literal : " + EXPR_PATTERN);
              System.out.println("Pattern length  : " + EXPR_PATTERN.length());
              System.out.println("Input           : " + template);
              System.out.println("Input length    : " + template.length());
              System.out.println("----- MATCH -----");

              Pattern pattern = Pattern.compile(EXPR_PATTERN);
              Matcher m = pattern.matcher(template);

              if (!m.find()) {
                System.out.println("NO MATCH");
                return;
              }

              String token = m.group(0);
              String name = m.group(1) != null ? m.group(1) : m.group(2);
              String rawRegex = m.group(3);

              System.out.println("group(0) token : " + token);
              System.out.println("group(1)      : " + m.group(1));
              System.out.println("group(2) name : " + name);
              System.out.println("group(3) raw  : " + rawRegex);

              String regex = rawRegex == null ? "" : rawRegex.trim();
              System.out.println("Final extracted regex: " + regex);

              // Quick check
              testValue(regex, "12345678"); // true
              testValue(regex, "123456789012"); // true
              testValue(regex, "1234");
            });
  }

  private static void testValue(String regex, String value) {
    boolean ok = value.matches(regex);
    System.out.printf("Value %-12s matches %s ? %s%n", "'" + value + "'", regex, ok);
  }
}

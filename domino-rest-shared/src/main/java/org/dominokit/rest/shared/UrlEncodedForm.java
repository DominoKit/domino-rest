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
package org.dominokit.rest.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Raw fields of an {@code application/x-www-form-urlencoded} request. Encoding is done by the
 * platform request, so the same fields can be sent from the browser and from the JVM.
 */
public class UrlEncodedForm {

  private final List<Field> fields = new ArrayList<>();

  /**
   * Adds a field. A null value is skipped, so the parameter stays absent. The same name may be
   * appended more than once.
   *
   * @param name the field name
   * @param value the raw field value
   * @return same instance
   */
  public UrlEncodedForm append(String name, String value) {
    if (value == null) {
      return this;
    }
    fields.add(new Field(name, value));
    return this;
  }

  /**
   * @return the fields in append order
   */
  public List<Field> getFields() {
    return fields;
  }

  /** One raw name/value pair. */
  public static class Field {

    private final String name;
    private final String value;

    private Field(String name, String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * @return the field name
     */
    public String name() {
      return name;
    }

    /**
     * @return the raw field value
     */
    public String value() {
      return value;
    }
  }
}

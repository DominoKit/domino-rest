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


package javax.ws.rs.core;

import java.util.LinkedHashMap;
import java.util.List;


public class Form {
  private final MultivaluedMap<String, String> parameters;

  
  public Form() {
    this(
        new AbstractMultivaluedMap<String, String>(new LinkedHashMap<String, List<String>>()) {
          // by default, the items in a Form are iterable based on their insertion order.
        });
  }

  
  public Form(final String parameterName, final String parameterValue) {
    this();

    parameters.add(parameterName, parameterValue);
  }

  
  public Form(final MultivaluedMap<String, String> store) {
    this.parameters = store;
  }

  
  public Form param(final String name, final String value) {
    parameters.add(name, value);

    return this;
  }

  
  public MultivaluedMap<String, String> asMap() {
    return parameters;
  }
}

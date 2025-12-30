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
package org.dominokit.rest.processor;

import java.util.Optional;
import javax.lang.model.element.ExecutableElement;

/** A helper class represents accessor method */
public class AccessorInfo {

  private final Optional<ExecutableElement> method;
  private String name;

  /**
   * Constructs an AccessorInfo from an {@link ExecutableElement}.
   *
   * @param method the method element
   */
  public AccessorInfo(ExecutableElement method) {
    this.method = Optional.of(method);
  }

  /**
   * Constructs an AccessorInfo from a name.
   *
   * @param name the name of the accessor
   */
  public AccessorInfo(String name) {
    this.name = name;
    this.method = Optional.empty();
  }

  /**
   * @return the name of the method
   */
  public String getName() {
    if (method.isPresent()) {
      return method.get().getSimpleName().toString();
    }
    return name;
  }

  /**
   * @return the method element
   */
  public Optional<ExecutableElement> getMethod() {
    return method;
  }
}

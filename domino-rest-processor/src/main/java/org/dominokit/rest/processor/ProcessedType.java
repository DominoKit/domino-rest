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

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/** Indicates that the {@code enclosingType} and {@code methods} are processed */
public class ProcessedType {

  private final TypeElement enclosingType;
  private final List<ExecutableElement> methods = new ArrayList<>();
  private final Elements elements;

  /**
   * Creates a new instance.
   *
   * @param elements the {@link Elements} utility
   * @param enclosingType the enclosing {@link TypeElement}
   */
  public ProcessedType(Elements elements, TypeElement enclosingType) {
    this.elements = elements;
    this.enclosingType = enclosingType;
  }

  /**
   * Adds method to be considered as processed
   *
   * @param method a processed method
   */
  public void addMethod(ExecutableElement method) {
    methods.add(method);
  }

  /**
   * @param targetMethod the method
   * @return true if {@code targetMethod} overrides one of the processed methods, false otherwise
   */
  public boolean overrides(ExecutableElement targetMethod) {
    for (ExecutableElement method : methods) {
      if (elements.overrides(method, targetMethod, enclosingType)) {
        return true;
      }
    }

    return false;
  }
}

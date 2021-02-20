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
package org.dominokit.domino.rest.apt;

import dominojackson.shaded.org.dominokit.domino.apt.commons.ProcessorUtil;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/** Helper class for an element */
public class BeanAccessors {

  private Map<String, AccessorInfo> getters = new HashMap<>();
  private Element beanElement;
  private ProcessorUtil processorUtil;

  public BeanAccessors(ProcessorUtil processorUtil, Element beanElement) {
    this.processorUtil = processorUtil;
    this.beanElement = beanElement;
    getters.putAll(getAccessors((TypeElement) beanElement));
  }

  AccessorInfo getterInfo(Element field) {
    final String upperCaseFirstLetter =
        processorUtil.capitalizeFirstLetter(field.getSimpleName().toString());
    String prefix = field.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get";
    if (getters.containsKey(prefix + upperCaseFirstLetter)) {
      return getters.get(prefix + upperCaseFirstLetter);
    } else {
      return new AccessorInfo(field.getSimpleName().toString());
    }
  }

  private Map<String, AccessorInfo> getAccessors(TypeElement element) {
    Map<String, AccessorInfo> getters = new HashMap<>();

    TypeMirror superclass = element.getSuperclass();
    if (superclass.getKind().equals(TypeKind.NONE)) {
      return getters;
    }

    element.getEnclosedElements().stream()
        .filter(
            e ->
                ElementKind.METHOD.equals(e.getKind())
                    && !e.getModifiers().contains(Modifier.STATIC)
                    && e.getModifiers().contains(Modifier.PUBLIC))
        .map(e -> new AccessorInfo((ExecutableElement) e))
        .forEach(accessorInfo -> getters.put(accessorInfo.getName(), accessorInfo));
    getters.putAll(getAccessors((TypeElement) processorUtil.getTypes().asElement(superclass)));
    return getters;
  }
}

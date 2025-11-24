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

import dominojackson.shaded.com.squareup.javapoet.*;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.dominokit.rest.shared.request.MetaParam;

public class MetaParamUtil {

  /**
   * Convert a full AnnotationMirror into a root MetaParam. The annotation itself will have name =
   * FQN, value = FQN.
   */
  public static MetaParam fromAnnotationMirror(AnnotationMirror mirror) {
    TypeElement annType = (TypeElement) mirror.getAnnotationType().asElement();
    String fqn = annType.getQualifiedName().toString();

    MetaParam annotationMeta = MetaParam.of(fqn, fqn);

    // Parameters of the annotation
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
        mirror.getElementValues().entrySet()) {

      String paramName = entry.getKey().getSimpleName().toString();
      AnnotationValue paramValue = entry.getValue();

      annotationMeta.addMetaParam(fromAnnotationValue(paramName, paramValue));
    }

    return annotationMeta;
  }

  /**
   * Convert a single annotation parameter (AnnotationValue) to MetaParam, recursively handling
   * nested annotations and arrays.
   */
  @SuppressWarnings("unchecked")
  private static MetaParam fromAnnotationValue(String name, AnnotationValue value) {
    Object v = value.getValue();

    // Nested annotation parameter
    if (v instanceof AnnotationMirror) {
      AnnotationMirror nested = (AnnotationMirror) v;

      // Parent param = parameter name, value = nested annotation FQN
      TypeElement nestedType = (TypeElement) nested.getAnnotationType().asElement();
      String nestedFqn = nestedType.getQualifiedName().toString();

      MetaParam paramMeta = MetaParam.of(name, nestedFqn);
      // Add the nested annotation as a child MetaParam
      paramMeta.addMetaParam(fromAnnotationMirror(nested));
      return paramMeta;
    }

    // Array parameter (e.g. String[], SomeAnn[], enums[])
    if (v instanceof List) {
      List<AnnotationValue> values = (List<AnnotationValue>) v;

      // The array itself as one MetaParam (stringified)
      MetaParam arrayMeta = MetaParam.of(name, valueToString(values));

      // Also add each element as a nested MetaParam if you want more structure
      for (AnnotationValue element : values) {
        Object ev = element.getValue();
        if (ev instanceof AnnotationMirror) {
          arrayMeta.addMetaParam(fromAnnotationMirror((AnnotationMirror) ev));
        } else {
          arrayMeta.addMetaParam(MetaParam.of(name, valueToString(ev)));
        }
      }
      return arrayMeta;
    }

    // Simple values: primitives, String, enum, class literals...
    return MetaParam.of(name, valueToString(v));
  }

  /** Turn an annotation parameter value into a reasonable String. */
  private static String valueToString(Object v) {
    if (v == null) {
      return "null";
    }

    // String literal
    if (v instanceof String) {
      return (String) v;
    }

    // Class literal (e.g. MyType.class)
    if (v instanceof TypeMirror) {
      return v.toString();
    }

    // Enum constant
    if (v instanceof VariableElement) {
      VariableElement ve = (VariableElement) v;
      Element enclosing = ve.getEnclosingElement();
      if (enclosing instanceof TypeElement) {
        String enumFqn = ((TypeElement) enclosing).getQualifiedName().toString();
        return enumFqn + "." + ve.getSimpleName().toString();
      }
      return ve.getSimpleName().toString();
    }

    // Arrays when passed directly here
    if (v instanceof List) {
      @SuppressWarnings("unchecked")
      List<AnnotationValue> list = (List<AnnotationValue>) v;
      return valueToString(list);
    }

    // Primitive wrappers, etc.
    return String.valueOf(v);
  }

  private static String valueToString(List<AnnotationValue> values) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    for (AnnotationValue av : values) {
      if (!first) {
        sb.append(", ");
      }
      first = false;
      sb.append(valueToString(av.getValue()));
    }
    sb.append("]");
    return sb.toString();
  }

  public static CodeBlock toCodeBlock(MetaParam metaParam, CodeBlock.Builder codeBlock) {

    codeBlock.add(
        "$T.of($S, $S)", TypeName.get(MetaParam.class), metaParam.getName(), metaParam.getValue());
    if (!metaParam.getMetaParams().isEmpty()) {
      codeBlock.add("\n");
      codeBlock.indent();
    }
    metaParam
        .getMetaParams()
        .forEach(
            mp -> {
              codeBlock.add(".addMetaParam(");
              codeBlock.add(toCodeBlock(mp, CodeBlock.builder()));
              codeBlock.add(")");
            });
    if (!metaParam.getMetaParams().isEmpty()) {
      codeBlock.unindent();
    }

    return codeBlock.build();
  }
}

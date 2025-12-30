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

import java.util.List;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;

public class ResourceLocatorFilter {

  public static boolean isResourceLocator(ExecutableElement method) {

    // 1) must have @Path on the method
    if (!hasPathAnnotation(method)) {
      return false;
    }

    // 2) must NOT be an HTTP handler
    if (hasDirectHttpMethodAnnotation(method) || hasMetaHttpMethodAnnotation(method)) {
      return false;
    }

    // 3) must return something (not void)
    TypeMirror returnType = method.getReturnType();
    if (isVoid(returnType)) {
      return false;
    }

    // 4) return type must itself be a resource
    return isResourceType(returnType);
  }

  private static boolean isResourceType(TypeMirror type) {
    // Must be a declared type (class/interface)
    if (type.getKind() != TypeKind.DECLARED) {
      return false;
    }

    DeclaredType declared = (DeclaredType) type;
    Element el = declared.asElement();
    if (!(el instanceof TypeElement)) {
      return false;
    }

    TypeElement typeElement = (TypeElement) el;

    // A) If class has @Path, it's definitely a resource
    if (hasPathAnnotation(typeElement)) {
      return true;
    }

    // B) Otherwise, check if any method makes it a resource/sub-resource
    List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());

    for (ExecutableElement m : methods) {
      if (hasPathAnnotation(m)) {
        return true; // sub-resource method exists
      }
      if (hasDirectHttpMethodAnnotation(m) || hasMetaHttpMethodAnnotation(m)) {
        return true; // request handler exists
      }
    }

    return false;
  }

  private static boolean hasPathAnnotation(Element element) {
    return hasAnnotation(element, "javax.ws.rs.Path", "jakarta.ws.rs.Path");
  }

  private static boolean hasDirectHttpMethodAnnotation(Element element) {
    return hasAnnotation(
        element,
        "javax.ws.rs.GET",
        "jakarta.ws.rs.GET",
        "javax.ws.rs.POST",
        "jakarta.ws.rs.POST",
        "javax.ws.rs.PUT",
        "jakarta.ws.rs.PUT",
        "javax.ws.rs.DELETE",
        "jakarta.ws.rs.DELETE",
        "javax.ws.rs.HEAD",
        "jakarta.ws.rs.HEAD",
        "javax.ws.rs.OPTIONS",
        "jakarta.ws.rs.OPTIONS",
        "javax.ws.rs.PATCH",
        "jakarta.ws.rs.PATCH");
  }

  private static boolean hasMetaHttpMethodAnnotation(ExecutableElement method) {
    for (AnnotationMirror am : method.getAnnotationMirrors()) {
      Element annElement = am.getAnnotationType().asElement();
      if (annElement instanceof TypeElement) {
        if (hasAnnotation(annElement, "javax.ws.rs.HttpMethod", "jakarta.ws.rs.HttpMethod")) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean hasAnnotation(Element element, String... fqns) {
    for (AnnotationMirror am : element.getAnnotationMirrors()) {
      String annFqn =
          ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString();
      for (String fqn : fqns) {
        if (annFqn.equals(fqn)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isVoid(TypeMirror type) {
    return type.getKind() == TypeKind.VOID;
  }
}

/*
 * Copyright © 2018 The GWT Authors
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

import dominojackson.shaded.org.dominokit.domino.apt.commons.AbstractProcessingStep;
import dominojackson.shaded.org.dominokit.domino.apt.commons.ExceptionUtil;
import dominojackson.shaded.org.dominokit.domino.apt.commons.StepBuilder;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.dominokit.rest.shared.request.service.annotations.MetaDataAnnotations;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.rest.shared.request.service.annotations.ResourceList;

/**
 * A processing step to generating factories for all {@link RequestFactory} listed in {@link
 * ResourceList}
 */
public class ResourceListProcessingStep extends AbstractProcessingStep {

  public ResourceListProcessingStep(ProcessingEnvironment processingEnv) {
    super(processingEnv);
  }

  public static class Builder extends StepBuilder<ResourceListProcessingStep> {

    public ResourceListProcessingStep build() {
      return new ResourceListProcessingStep(processingEnv);
    }
  }

  /**
   * Process and generate factories for {@code elementsByAnnotation}
   *
   * @param elementsByAnnotation the annotated elements
   */
  public void process(Set<? extends Element> elementsByAnnotation) {

    for (Element element : elementsByAnnotation) {
      try {
        generateFactory(element);
      } catch (Exception e) {
        ExceptionUtil.messageStackTrace(messager, e);
      }
    }
  }

  private void generateFactory(Element resourceListElement) {
    ResourceList resourceList = resourceListElement.getAnnotation(ResourceList.class);
    Set<TypeMirror> resourceTypes =
        new HashSet<>(
            processorUtil.getClassArrayValueFromAnnotation(
                resourceListElement, ResourceList.class, "value"));
    Set<TypeMirror> metadataAnnotationTypes =
        new HashSet<>(
            processorUtil.getClassArrayValueFromAnnotation(
                resourceListElement, MetaDataAnnotations.class, "value"));

    resourceTypes.forEach(
        typeMirror -> {
          Element element = types.asElement(typeMirror);
          writeSource(
              new RequestFactorySourceWriter(
                      element, resourceList.serviceRoot(), metadataAnnotationTypes, processingEnv)
                  .asTypeBuilder(),
              elements.getPackageOf(resourceListElement).getQualifiedName().toString());
        });
  }
}

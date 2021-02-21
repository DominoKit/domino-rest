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
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

/** A step to generating factories for all {@link RequestFactory} listed in source path */
public class RequestFactoryProcessingStep extends AbstractProcessingStep {

  public RequestFactoryProcessingStep(ProcessingEnvironment processingEnv) {
    super(processingEnv);
  }

  public static class Builder extends StepBuilder<RequestFactoryProcessingStep> {

    public RequestFactoryProcessingStep build() {
      return new RequestFactoryProcessingStep(processingEnv);
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

  private void generateFactory(Element serviceElement) {
    writeSource(
        new RequestFactorySourceWriter(serviceElement, processingEnv).asTypeBuilder(),
        elements.getPackageOf(serviceElement).getQualifiedName().toString());
  }
}

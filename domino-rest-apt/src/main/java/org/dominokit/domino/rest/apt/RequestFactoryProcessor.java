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

import dominojackson.shaded.com.google.auto.service.AutoService;
import dominojackson.shaded.org.dominokit.domino.apt.commons.BaseProcessor;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.domino.rest.shared.request.service.annotations.ResourceList;

/**
 * A processor which generates the client for all {@link RequestFactory} defined in the source path
 */
@AutoService(Processor.class)
public class RequestFactoryProcessor extends BaseProcessor {

  private final Set<String> supportedAnnotations = new HashSet<>();

  public RequestFactoryProcessor() {
    supportedAnnotations.add(RequestFactory.class.getCanonicalName());
    supportedAnnotations.add(ResourceList.class.getCanonicalName());
  }

  /** {@inheritDoc} */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return supportedAnnotations;
  }

  /** {@inheritDoc} */
  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /** {@inheritDoc} */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    new RequestFactoryProcessingStep.Builder()
        .setProcessingEnv(processingEnv)
        .build()
        .process(
            roundEnv.getElementsAnnotatedWith(RequestFactory.class).stream()
                .filter(e -> ElementKind.INTERFACE.equals(e.getKind()))
                .collect(Collectors.toSet()));

    new ResourceListProcessingStep.Builder()
        .setProcessingEnv(processingEnv)
        .build()
        .process(
            roundEnv.getElementsAnnotatedWith(ResourceList.class).stream()
                .filter(e -> ElementKind.PACKAGE.equals(e.getKind()))
                .collect(Collectors.toSet()));
    return false;
  }
}

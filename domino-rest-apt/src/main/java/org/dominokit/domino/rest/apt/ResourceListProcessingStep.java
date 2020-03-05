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
package org.dominokit.domino.rest.apt;

import dominojackson.shaded.org.dominokit.domino.apt.commons.AbstractProcessingStep;
import dominojackson.shaded.org.dominokit.domino.apt.commons.ExceptionUtil;
import dominojackson.shaded.org.dominokit.domino.apt.commons.StepBuilder;
import org.dominokit.domino.rest.shared.request.service.annotations.ResourceList;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

public class ResourceListProcessingStep extends AbstractProcessingStep {


    public ResourceListProcessingStep(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public static class Builder extends StepBuilder<ResourceListProcessingStep> {

        public ResourceListProcessingStep build() {
            return new ResourceListProcessingStep(processingEnv);
        }
    }

    public void process(
            Set<? extends Element> elementsByAnnotation) {

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
        List<TypeMirror> resourceTypes = processorUtil.getClassArrayValueFromAnnotation(resourceListElement, ResourceList.class, "value");
        resourceTypes.forEach(typeMirror -> {
            Element element = types.asElement(typeMirror);
            writeSource(new RequestFactorySourceWriter(element, resourceList.serviceRoot(), processingEnv).asTypeBuilder(), elements.getPackageOf(resourceListElement).getQualifiedName().toString());
        });

    }

}

package org.dominokit.domino.rest.apt;

import dominojackson.shaded.com.google.auto.service.AutoService;
import dominojackson.shaded.org.dominokit.domino.apt.commons.BaseProcessor;
import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.domino.rest.shared.request.service.annotations.ResourceList;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@AutoService(Processor.class)
public class RequestFactoryProcessor extends BaseProcessor {

    private final Set<String> supportedAnnotations = new HashSet<>();

    public RequestFactoryProcessor() {
        supportedAnnotations.add(RequestFactory.class.getCanonicalName());
        supportedAnnotations.add(ResourceList.class.getCanonicalName());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        new RequestFactoryProcessingStep.Builder()
                .setProcessingEnv(processingEnv)
                .build()
                .process(roundEnv.getElementsAnnotatedWith(RequestFactory.class)
                        .stream().filter(e -> ElementKind.INTERFACE.equals(e.getKind()))
                        .collect(Collectors.toSet()));

        new ResourceListProcessingStep.Builder()
                .setProcessingEnv(processingEnv)
                .build()
                .process(roundEnv.getElementsAnnotatedWith(ResourceList.class)
                        .stream().filter(e -> ElementKind.PACKAGE.equals(e.getKind()))
                        .collect(Collectors.toSet()));
        return false;
    }
}

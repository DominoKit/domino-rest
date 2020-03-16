package org.dominokit.domino.rest.apt;

import dominojackson.shaded.org.dominokit.domino.apt.commons.ProcessorUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        final String upperCaseFirstLetter = processorUtil.capitalizeFirstLetter(field.getSimpleName().toString());
        String prefix = field.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get";
        if(getters.containsKey(prefix + upperCaseFirstLetter)){
            return getters.get(prefix + upperCaseFirstLetter);
        }else{
            return new AccessorInfo(field.getSimpleName().toString());
        }
    }

    private Map<String, AccessorInfo> getAccessors(TypeElement element) {
        Map<String, AccessorInfo> getters = new HashMap<>();

        TypeMirror superclass = element.getSuperclass();
        if (superclass.getKind().equals(TypeKind.NONE)) {
            return getters;
        }

        element
                .getEnclosedElements()
                .stream()
                .filter(e -> ElementKind.METHOD.equals(e.getKind()) &&
                        !e.getModifiers().contains(Modifier.STATIC) &&
                        e.getModifiers().contains(Modifier.PUBLIC))
                .map(e -> new AccessorInfo(Optional.of((ExecutableElement) e)))
                .forEach(accessorInfo -> getters.put(accessorInfo.getName(), accessorInfo));
        getters.putAll(getAccessors((TypeElement) processorUtil.getTypes().asElement(superclass)));
        return getters;
    }
}

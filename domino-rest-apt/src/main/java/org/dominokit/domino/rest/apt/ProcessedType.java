package org.dominokit.domino.rest.apt;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class ProcessedType {

    private final TypeElement enclosingType;
    private final List<ExecutableElement> methods = new ArrayList<>();
    private final Elements elements;

    public ProcessedType(Elements elements, TypeElement enclosingType) {
        this.elements = elements;
        this.enclosingType = enclosingType;
    }

    public void addMethod(ExecutableElement method){
        methods.add(method);
    }

    public boolean overrides(ExecutableElement targetMethod){
        for (ExecutableElement method : methods) {
            if(elements.overrides(method, targetMethod, enclosingType)){
                return true;
            }
        }

        return false;
    }
}

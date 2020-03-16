package org.dominokit.domino.rest.apt;

import javax.lang.model.element.ExecutableElement;
import java.util.Optional;

public class AccessorInfo {

        public Optional<ExecutableElement> method;
        private String name;

        public AccessorInfo(Optional<ExecutableElement> method) {
            this.method = method;
        }

        public AccessorInfo(String name) {
            this.name = name;
            this.method = Optional.empty();
        }

        public String getName(){
            if(method.isPresent()){
                return method.get().getSimpleName().toString();
            }
            return name;
        }
    }
package org.dominokit.domino.rest.shared.request.service.annotations;

public @interface DateFormat {
    String value() default "dd-MM-yyyy";
}

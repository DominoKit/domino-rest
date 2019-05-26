package org.dominokit.domino.rest.apt;

import com.squareup.javapoet.CodeBlock;
import org.dominokit.domino.apt.commons.ExceptionUtil;
import org.dominokit.domino.history.StateHistoryToken;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class ReplaceParametersMethodBuilder {
    private final ExecutableElement method;
    private final Messager messager;
    private final String path;

    public ReplaceParametersMethodBuilder(Messager messager, String path, ExecutableElement method) {
        this.messager = messager;
        this.path = path;
        this.method =method;
    }

    CodeBlock build() {
        CodeBlock.Builder replacerBuilder = CodeBlock.builder().beginControlFlow("setRequestParametersReplacer((token, bean) -> ");

        try {

            CodeBlock.Builder bodyBuilder = CodeBlock.builder()
                    .beginControlFlow("if (token.value().contains(\":\"))");
            StateHistoryToken token = new StateHistoryToken(path);
            Set<String> methodParams = this.method.getParameters().stream().map(methodParam -> methodParam.getSimpleName().toString())
                    .collect(Collectors.toSet());

            token.paths()
                    .stream()
                    .filter(tokenPath -> tokenPath.startsWith(":") && !methodParams.contains(tokenPath.replace(":","")))
                    .forEach(tokenPath -> bodyBuilder.addStatement("token.replacePath(\"" + tokenPath + "\", " + convertParameterToGetter(tokenPath.replace(":", "")) + "+\"\")", Objects.class));

            token.queryParameters()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().startsWith(":"))
                    .forEach(entry -> {
                        bodyBuilder.addStatement("token.replaceParameter(\"" + entry.getKey() + "\", \"" + entry.getKey() + "\", " + convertParameterToGetter(entry.getValue().replace(":", "")) + "+\"\")", Objects.class);
                    });

            token.fragments()
                    .stream()
                    .filter(fragment -> fragment.startsWith(":"))
                    .forEach(fragment -> bodyBuilder.addStatement("token.replaceFragment(\"" + fragment + "\", " + convertParameterToGetter(fragment.replace(":", "")) + "+\"\")", Objects.class));

            bodyBuilder.endControlFlow();
            replacerBuilder
                    .add(CodeBlock.builder()
                            .add(bodyBuilder.build())
                            .addStatement("return token.value()")
                            .build());
            replacerBuilder.endControlFlow(")");

            return replacerBuilder.build();
        } catch (Exception ex) {
            ExceptionUtil.messageStackTrace(messager, ex);
        }

        return replacerBuilder.build();
    }

    private String convertParameterToGetter(String parameter) {
        String names = parameter
                .replace("{", "")
                .replace("}", "");

        String[] fieldsNames = names.contains(".") ? names.split("\\.") : new String[]{names};

        List<String> gettersString = Arrays.asList(fieldsNames)
                .stream()
                .map(s -> "get" + s.replaceFirst(s.charAt(0) + "", (s.charAt(0) + "").toUpperCase()) + "()")
                .collect(Collectors.toList());

        String result = getterWithNullCheck(gettersString, 1);

        return result;
    }

    private String getterWithNullCheck(List<String> getters, int upTo) {

        if (upTo <= getters.size()) {
            return "$1T.isNull(bean." + getters.subList(0, upTo).stream().collect(Collectors.joining(".")) + ")?null:(" + getterWithNullCheck(getters, ++upTo) + ")";
        } else {
            return "bean." + getters.subList(0, upTo - 1).stream().collect(Collectors.joining("."));
        }
    }
}

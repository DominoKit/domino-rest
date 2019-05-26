package org.dominokit.domino.rest.apt;


import com.squareup.javapoet.*;
import org.dominokit.domino.apt.commons.AbstractSourceBuilder;
import org.dominokit.domino.apt.commons.DominoTypeBuilder;
import org.dominokit.domino.rest.shared.request.*;
import org.dominokit.domino.rest.shared.request.service.annotations.*;
import org.dominokit.domino.rest.shared.request.service.annotations.Request;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class RequestFactorySourceWriter extends AbstractSourceBuilder {

    private final Element serviceElement;
    private final String requestsServiceRoot;

    public RequestFactorySourceWriter(Element serviceElement, ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
        this.serviceElement = serviceElement;
        this.requestsServiceRoot = serviceElement.getAnnotation(RequestFactory.class).serviceRoot();
    }

    @Override
    public List<TypeSpec.Builder> asTypeBuilder() {
        String factoryName = serviceElement.getSimpleName().toString() + "Factory";

        FieldSpec instanceField = FieldSpec.builder(ClassName.bestGuess(factoryName), "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new " + factoryName + "()")
                .build();

        List<ExecutableElement> serviceMethods = processorUtil
                .getElementMethods(serviceElement);
        List<TypeSpec> requests = serviceMethods
                .stream()
                .map(this::makeRequestClass)
                .collect(toList());

        List<MethodSpec> overrideMethods = serviceMethods
                .stream()
                .map(this::makeRequestFactoryMethod)
                .collect(toList());

        TypeSpec.Builder factory = DominoTypeBuilder.classBuilder(factoryName, RequestFactoryProcessor.class)
                .addSuperinterface(TypeName.get(serviceElement.asType()))
                .addField(instanceField)
                .addTypes(requests)
                .addMethods(overrideMethods);


        return Collections.singletonList(factory);
    }

    private MethodSpec makeRequestFactoryMethod(ExecutableElement method) {
        TypeName requestTypeName = TypeName.get(getRequestBeanType(method));
        TypeMirror responseBean = getResponseBeanType(method);


        MethodSpec.Builder request = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ServerRequest.class), requestTypeName, ClassName.get(responseBean)));

        method.getParameters()
                .forEach(parameter -> request.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString()));

        String requestClassName = serviceElement.getSimpleName().toString() + "_" + method.getSimpleName();
        String initializeStatement = requestClassName + " instance = new " + requestClassName;

        Optional<String> requestBodyParamName = getRequestBodyParamName(method);

        if (requestBodyParamName.isPresent()) {
            request.addStatement(initializeStatement + "(" + requestBodyParamName.get() + ")");
        } else
            request.addStatement(initializeStatement + "($T.VOID_REQUEST)", RequestBean.class);

        method.getParameters()
                .stream()
                .filter(parameter -> !isBodyParameter(parameter))
                .forEach(parameter -> request.addStatement("instance.addCallArgument($S, String.valueOf($L))", parameter.getSimpleName().toString(), parameter.getSimpleName().toString()));

        request.addStatement("return instance");

        return request.build();
    }

    private TypeSpec makeRequestClass(ExecutableElement method) {

        TypeName requestTypeName = TypeName.get(getRequestBeanType(method));
        TypeMirror responseBean = getResponseBeanType(method);

        TypeSpec.Builder requestBuilder = TypeSpec
                .classBuilder(serviceElement.getSimpleName().toString() + "_" + method.getSimpleName())
                .addAnnotation(Request.class)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(ServerRequest.class),
                        requestTypeName,
                        ClassName.get(responseBean)))
                .addMethod(constructor(requestTypeName, method));

        return requestBuilder.build();
    }

    private TypeMirror getResponseBeanType(ExecutableElement method) {
        DeclaredType responseReturnType = (DeclaredType) method.getReturnType();
        return responseReturnType.getTypeArguments().get(0);
    }


    private TypeMirror getRequestBeanType(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();

        return parameters.stream()
                .filter(this::isBodyParameter)
                .map(Element::asType)
                .findFirst()
                .orElse(elements.getTypeElement(VoidRequest.class.getCanonicalName()).asType());
    }

    private Optional<String> getRequestBodyParamName(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();

        return parameters.stream()
                .filter(this::isBodyParameter)
                .map(parameter -> parameter.getSimpleName().toString())
                .findFirst();
    }


    private boolean isBodyParameter(VariableElement parameter) {
        return isRequestBean(parameter) || isRequestBody(parameter);
    }

    private boolean isRequestBody(VariableElement parameter) {
        return nonNull(parameter.getAnnotation(RequestBody.class)) ||
                nonNull(parameter.asType().getAnnotation(RequestBody.class));
    }

    private boolean isRequestBean(VariableElement parameter) {
        return processorUtil.isAssignableFrom(parameter, RequestBean.class);
    }

    private MethodSpec constructor(TypeName requestBean, ExecutableElement method) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addParameter(requestBean, "request")
                .addStatement("super(request)")
                .addStatement("setHttpMethod($S)", getHttpMethod(method))
                .addStatement("setContentType(new String[]{$L})", getContentType(method))
                .addStatement("setAccept(new String[]{$L})", getAcceptResponse(method))
                .addStatement("setPath($S)", getPath(method))
                .addStatement("setServiceRoot($S)", getServiceRoot(method));

        if (nonNull(method.getAnnotation(SuccessCodes.class))) {
            constructorBuilder.addStatement("setSuccessCodes(new Integer[]{$L})", getSuccessCodes(method));
        }

        if (isVoidResponse(method)) {
            constructorBuilder.addStatement("markAsVoidResponse()");
        }

        if (!isVoidRequest(method)) {
            constructorBuilder.addCode(getRequestWriter(method));
        }

        if (!isVoidResponse(method)) {
            constructorBuilder.addCode(getResponseReader(method));
        }

        if (!isVoidRequest(method)) {
            constructorBuilder.addCode(new ReplaceParametersMethodBuilder(messager, getPath(method), method).build());
        }

        return constructorBuilder.build();
    }

    private CodeBlock getRequestWriter(ExecutableElement method) {
        CodeBlock.Builder builder = CodeBlock.builder();

        if (processorUtil.isStringType(getRequestBeanType(method))) {
            builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", TypeName.get(StringWriter.class));
        } else {
            Writer annotation = method.getAnnotation(Writer.class);
            if (nonNull(annotation)) {
                Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(method, Writer.class, "value");
                value.ifPresent(writerType -> builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", TypeName.get(writerType)));
            } else {
                Element requestType = types.asElement(getRequestBeanType(method));
                builder.addStatement("setRequestWriter(bean -> $T.INSTANCE.write(bean))", mapperClassName(requestType));
            }
        }

        return builder.build();
    }

    private CodeBlock getResponseReader(ExecutableElement method) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Reader annotation = method.getAnnotation(Reader.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(method, Reader.class, "value");
            value.ifPresent(readerType -> builder.addStatement("setResponseReader(response -> new $T().read(response))", TypeName.get(readerType)));
        } else {
            TypeMirror responseBeanType = getResponseBeanType(method);
            if (processorUtil.isAssignableFrom(responseBeanType, ArrayResponse.class)) {
                TypeMirror beanType = ((DeclaredType) responseBeanType).getTypeArguments().get(0);
                builder.addStatement("setResponseReader(response -> new $T<>($T.INSTANCE.readArray(response, length -> new $T[length])))", TypeName.get(ArrayResponse.class), mapperClassName(types.asElement(beanType)), TypeName.get(beanType));
            } else {
                builder.addStatement("setResponseReader(response -> $T.INSTANCE.read(response))", mapperClassName(types.asElement(responseBeanType)));
            }
        }

        return builder.build();
    }

    private ClassName mapperClassName(Element type) {
        return ClassName.get(elements.getPackageOf(type).getQualifiedName().toString(), type.getSimpleName().toString() + "_MapperImpl");
    }

    private boolean isVoidResponse(ExecutableElement method) {
        return types.isSameType(getResponseBeanType(method), elements.getTypeElement(VoidResponse.class.getCanonicalName()).asType());
    }

    private boolean isVoidRequest(ExecutableElement method) {
        return types.isSameType(getRequestBeanType(method), elements.getTypeElement(VoidRequest.class.getCanonicalName()).asType());
    }

    private String getContentType(ExecutableElement method) {
        if (nonNull(method.getAnnotation(Produces.class))) {
            return Arrays.stream(method.getAnnotation(Produces.class).value()).map(s -> "\"" + s + "\"")
                    .collect(joining(","));
        } else {
            return "\"" + MediaType.APPLICATION_JSON + "\"";
        }
    }

    private String getAcceptResponse(ExecutableElement method) {
        if (nonNull(method.getAnnotation(Consumes.class))) {
            return Arrays.stream(method.getAnnotation(Consumes.class).value()).map(s -> "\"" + s + "\"")
                    .collect(joining(","));
        } else {
            return "\"" + MediaType.APPLICATION_JSON + "\"";
        }
    }

    private String getPath(ExecutableElement method) {
        return method.getAnnotation(Path.class).value();
    }

    private String getServiceRoot(ExecutableElement method) {
        if (nonNull(method.getAnnotation(ServiceRoot.class))) {
            return method.getAnnotation(ServiceRoot.class).value();
        } else {
            return requestsServiceRoot;
        }
    }

    private String getSuccessCodes(ExecutableElement method) {
        return IntStream.of(method.getAnnotation(SuccessCodes.class).value())
                .boxed()
                .map(String::valueOf)
                .collect(joining(","));
    }

    private void copyAnnotations(AnnotationSpec.Builder requestBuilder, ExecutableElement method) {
        Path path = method.getAnnotation(Path.class);

        requestBuilder.addMember("value", "$S", path.value());

        requestBuilder.addMember("httpMethod", "$S", getHttpMethod(method));

        if (nonNull(method.getAnnotation(SuccessCodes.class))) {
            requestBuilder.addMember("successCodes", "{$L}", IntStream.of(method.getAnnotation(SuccessCodes.class).value())
                    .boxed()
                    .map(String::valueOf)
                    .collect(joining(",")));
        }

    }


    private String getHttpMethod(ExecutableElement method) {

        if (nonNull(method.getAnnotation(GET.class))) {
            return HttpMethod.GET;
        }

        if (nonNull(method.getAnnotation(POST.class))) {
            return HttpMethod.POST;
        }

        if (nonNull(method.getAnnotation(PUT.class))) {
            return HttpMethod.PUT;
        }

        if (nonNull(method.getAnnotation(OPTIONS.class))) {
            return HttpMethod.OPTIONS;
        }

        if (nonNull(method.getAnnotation(DELETE.class))) {
            return HttpMethod.DELETE;
        }

        if (nonNull(method.getAnnotation(PATCH.class))) {
            return HttpMethod.PATCH;
        }

        if (nonNull(method.getAnnotation(HEAD.class))) {
            return HttpMethod.HEAD;
        }


        return HttpMethod.GET;
    }

}

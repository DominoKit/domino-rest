package org.dominokit.domino.rest.apt;

import com.squareup.javapoet.*;
import org.dominokit.domino.apt.commons.AbstractSourceBuilder;
import org.dominokit.domino.apt.commons.DominoTypeBuilder;
import org.dominokit.domino.rest.shared.request.RequestBean;
import org.dominokit.domino.rest.shared.request.ServerRequest;
import org.dominokit.domino.rest.shared.request.service.annotations.*;
import org.dominokit.jacksonapt.AbstractObjectReader;
import org.dominokit.jacksonapt.AbstractObjectWriter;
import org.dominokit.jacksonapt.JsonDeserializer;
import org.dominokit.jacksonapt.JsonSerializer;
import org.dominokit.jacksonapt.annotation.JSONMapper;
import org.dominokit.jacksonapt.processor.ObjectMapperProcessor;
import org.dominokit.jacksonapt.processor.Type;
import org.dominokit.jacksonapt.processor.deserialization.FieldDeserializersChainBuilder;
import org.dominokit.jacksonapt.processor.serialization.FieldSerializerChainBuilder;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
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

        ObjectMapperProcessor.elementUtils = elements;
        ObjectMapperProcessor.typeUtils = types;
        ObjectMapperProcessor.messager = messager;
        ObjectMapperProcessor.filer = filer;
    }

    @Override
    public List<TypeSpec.Builder> asTypeBuilder() {
        String factoryName = serviceElement.getSimpleName().toString() + "Factory";

        FieldSpec instanceField = FieldSpec.builder(ClassName.bestGuess(factoryName), "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new " + factoryName + "()")
                .build();

        Map<String, Integer> methodCount = new HashMap<>();

        List<ServiceMethod> serviceMethods = processorUtil
                .getElementMethods(serviceElement)
                .stream()
                .map(executableElement -> {
                    String name = executableElement.getSimpleName().toString();
                    if(hasClassifier(executableElement)){
                        return new ServiceMethod(executableElement, 0);
                    }else {
                        if (!methodCount.containsKey(name)) {
                            methodCount.put(name, 1);
                            return new ServiceMethod(executableElement, 0);
                        } else {
                            Integer index = methodCount.get(name);
                            methodCount.put(name, methodCount.get(name) + 1);
                            return new ServiceMethod(executableElement, index);
                        }
                    }
                }).collect(toList());

        List<TypeSpec> requests = serviceMethods
                .stream()
                .map(this::makeRequestClass)
                .collect(toList());

        List<MethodSpec> overrideMethods = serviceMethods
                .stream()
                .map(this::makeRequestFactoryMethod)
                .collect(toList());

        TypeSpec.Builder factory = DominoTypeBuilder.classBuilder(factoryName, RequestFactoryProcessor.class)
                .addAnnotation(AnnotationSpec.builder(RestService.class)
                        .addMember("value", "$T.class", serviceElement.asType())
                        .build())
                .addField(instanceField)
                .addTypes(requests)
                .addMethods(overrideMethods);


        return Collections.singletonList(factory);
    }

    private MethodSpec makeRequestFactoryMethod(ServiceMethod serviceMethod) {
        String classifier = getMethodClassifier(serviceMethod);

        TypeName requestTypeName = TypeName.get(getRequestBeanType(serviceMethod.method));
        TypeMirror responseBean = getResponseBeanType(serviceMethod.method);

        MethodSpec.Builder request = MethodSpec.methodBuilder(serviceMethod.method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ServerRequest.class), requestTypeName, ClassName.get(responseBean)));

        serviceMethod.method.getParameters()
                .forEach(parameter -> request.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString()));

        String requestClassName = serviceElement.getSimpleName().toString() + "_" + serviceMethod.method.getSimpleName() + classifier;
        String initializeStatement = requestClassName + " instance = new " + requestClassName;

        Optional<String> requestBodyParamName = getRequestBodyParamName(serviceMethod.method);

        if (requestBodyParamName.isPresent()) {
            request.addStatement(initializeStatement + "(" + requestBodyParamName.get() + ")");
        } else
            request.addStatement(initializeStatement + "()");

        serviceMethod.method.getParameters()
                .forEach(parameter -> request.addStatement("instance.addCallArgument($S, String.valueOf($L))", parameter.getSimpleName().toString(), parameter.getSimpleName().toString()));

        request.addStatement("return instance");

        return request.build();
    }

    private TypeSpec makeRequestClass(ServiceMethod serviceMethod) {
        String classifier = getMethodClassifier(serviceMethod);
        TypeMirror requestType = getRequestBeanType(serviceMethod.method);
        TypeName requestTypeName = TypeName.get(requestType);
        TypeMirror responseBean = getResponseBeanType(serviceMethod.method);

        TypeSpec.Builder requestBuilder = TypeSpec
                .classBuilder(serviceElement.getSimpleName().toString() + "_" + serviceMethod.method.getSimpleName() + classifier)
                .addAnnotation(Request.class)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(ServerRequest.class),
                        requestTypeName,
                        ClassName.get(responseBean)))
                .addMethod(constructor(requestType, serviceMethod.method));

        return requestBuilder.build();
    }

    private String getMethodClassifier(ServiceMethod serviceMethod) {
        String classifier;
        Classifier classifierAnnotation = serviceMethod.method.getAnnotation(Classifier.class);
        if (hasClassifier(serviceMethod.method)) {
            classifier = "_" + classifierAnnotation.value();
        } else {
            classifier = serviceMethod.index > 0 ? "_" + serviceMethod.index : "";
        }
        return classifier;
    }

    private boolean hasClassifier(ExecutableElement method) {
        Classifier classifierAnnotation = method.getAnnotation(Classifier.class);
        return nonNull(classifierAnnotation) && !classifierAnnotation.value().trim().isEmpty();
    }

    private TypeMirror getResponseBeanType(ExecutableElement method) {
        return getMappingType(method.getReturnType());
    }

    private TypeMirror getMappingType(TypeMirror returnType) {
        if (isPrimitive(returnType)) {
            return wrapperType(returnType);
        }

        if (TypeKind.VOID.equals(returnType.getKind())) {
            return elements.getTypeElement(Void.class.getCanonicalName()).asType();
        }

        if (Type.isArray(returnType)) {
            return returnType;
        }

        return returnType;
    }

    private TypeMirror wrapperType(TypeMirror type) {
        return types.boxedClass((PrimitiveType) type).asType();
    }


    private boolean isPrimitive(TypeMirror typeMirror) {
        return typeMirror.getKind().isPrimitive();
    }

    private TypeMirror getRequestBeanType(ExecutableElement method) {
        List<? extends VariableElement> parameters = method.getParameters();

        TypeMirror typeMirror = parameters.stream()
                .filter(this::isBodyParameter)
                .map(Element::asType)
                .findFirst()
                .orElse(elements.getTypeElement(Void.class.getCanonicalName())
                        .asType());

        return getMappingType(typeMirror);
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
        TypeMirror typeMirror = parameter.asType();
        if(isPrimitive(typeMirror)
                || Type.isArray(typeMirror)
                || Type.is2dArray(typeMirror)
                || Type.isEnum(typeMirror)
                || Type.isCollection(typeMirror)
                || Type.isPrimitiveArray(typeMirror)
                || Type.isIterable(typeMirror)){

            return nonNull(parameter.getAnnotation(RequestBody.class));
        }else{
            TypeElement typeElement = elements.getTypeElement(typeMirror.toString());
            RequestBody pojoAnnotation = typeElement.getAnnotation(RequestBody.class);
            JSONMapper mapperAnnotation = typeElement.getAnnotation(JSONMapper.class);
            return nonNull(parameter.getAnnotation(RequestBody.class)) ||
                    nonNull(pojoAnnotation) || nonNull(mapperAnnotation);
        }
    }

    private boolean isRequestBean(VariableElement parameter) {
        return processorUtil.isAssignableFrom(parameter, RequestBean.class);
    }

    private MethodSpec constructor(TypeMirror requestBean, ExecutableElement method) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        if (isVoidType(requestBean)) {
            constructorBuilder.addStatement("super(null)");
        } else {
            constructorBuilder
                    .addParameter(TypeName.get(requestBean), "request")
                    .addStatement("super(request)");
        }

        constructorBuilder.addStatement("setHttpMethod($S)", getHttpMethod(method))
                .addStatement("setContentType(new String[]{$L})", getContentType(method))
                .addStatement("setAccept(new String[]{$L})", getAcceptResponse(method))
                .addStatement("setPath($S)", getPath(method))
                .addStatement("setServiceRoot($S)", getServiceRoot(method));

        Retries retries = method.getAnnotation(Retries.class);
        if(nonNull(retries)) {
            constructorBuilder.addStatement("setTimeout($L)", retries.timeout());
            constructorBuilder.addStatement("setMaxRetries($L)", retries.maxRetries());
        }
        if (nonNull(method.getAnnotation(SuccessCodes.class))) {
            constructorBuilder.addStatement("setSuccessCodes(new Integer[]{$L})", getSuccessCodes(method));
        }

        if (!isVoidType(method)) {
            constructorBuilder.addCode(getRequestWriter(method));
        }

        constructorBuilder.addCode(getResponseReader(method));

        if (!isVoidType(method)) {
            constructorBuilder.addCode(new ReplaceParametersMethodBuilder(messager, getPath(method), method).build());
        }

        return constructorBuilder.build();
    }

    private CodeBlock getRequestWriter(ExecutableElement method) {
        CodeBlock.Builder builder = CodeBlock.builder();


        Writer annotation = method.getAnnotation(Writer.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(method, Writer.class, "value");
            value.ifPresent(writerType -> builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", TypeName.get(writerType)));
        } else {
            TypeMirror requestBeanType = getRequestBeanType(method);
            CodeBlock instance = new FieldSerializerChainBuilder(requestBeanType).getInstance(requestBeanType);
            TypeSpec.Builder writerType = TypeSpec.anonymousClassBuilder("$S", requestBeanType)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AbstractObjectWriter.class), TypeName.get(requestBeanType)))
                    .addMethod(MethodSpec.methodBuilder("newSerializer")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PROTECTED)
                            .returns(ParameterizedTypeName.get(ClassName.get(JsonSerializer.class), TypeName.get(requestBeanType)))
                            .addCode("return ")
                            .addCode(instance)
                            .addCode(";")
                            .build());

            builder.addStatement("setRequestWriter(bean -> $L.write(bean))", writerType.build());
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
            CodeBlock instance = new FieldDeserializersChainBuilder(responseBeanType).getInstance(responseBeanType);
            TypeSpec.Builder readerType = TypeSpec.anonymousClassBuilder("$S", responseBeanType)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AbstractObjectReader.class), TypeName.get(responseBeanType)))
                    .addMethod(MethodSpec.methodBuilder("newDeserializer")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PROTECTED)
                            .returns(ParameterizedTypeName.get(ClassName.get(JsonDeserializer.class), TypeName.get(responseBeanType)))
                            .addCode("return ")
                            .addCode(instance)
                            .addCode(";")
                            .build());

            builder.addStatement("setResponseReader(response -> $L.read(response))", readerType.build());
        }

        return builder.build();
    }

    private ClassName mapperClassName(Element type) {
        return ClassName.get(elements.getPackageOf(type).getQualifiedName().toString(), type.getSimpleName().toString() + "_MapperImpl");
    }

    private boolean isVoidType(ExecutableElement method) {
        return isVoidType(getRequestBeanType(method));
    }

    private boolean isVoidType(TypeMirror type) {
        return TypeKind.VOID.equals(type.getKind()) || types.isSameType(elements.getTypeElement(Void.class.getCanonicalName()).asType(), type);
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

    private static class ServiceMethod {
        private ExecutableElement method;
        private Integer index;

        public ServiceMethod(ExecutableElement method, Integer index) {
            this.method = method;
            this.index = index;
        }
    }

}

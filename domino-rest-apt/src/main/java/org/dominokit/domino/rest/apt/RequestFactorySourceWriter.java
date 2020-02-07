package org.dominokit.domino.rest.apt;

import com.squareup.javapoet.*;
import org.dominokit.domino.apt.commons.AbstractSourceBuilder;
import org.dominokit.domino.apt.commons.DominoTypeBuilder;
import org.dominokit.domino.rest.shared.request.*;
import org.dominokit.domino.rest.shared.request.service.annotations.*;
import org.dominokit.domino.rest.shared.request.service.annotations.Request;
import org.dominokit.jacksonapt.AbstractObjectReader;
import org.dominokit.jacksonapt.AbstractObjectWriter;
import org.dominokit.jacksonapt.JsonDeserializer;
import org.dominokit.jacksonapt.JsonSerializer;
import org.dominokit.jacksonapt.annotation.JSONMapper;
import org.dominokit.jacksonapt.annotation.JSONReader;
import org.dominokit.jacksonapt.annotation.JSONWriter;
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
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class RequestFactorySourceWriter extends AbstractSourceBuilder {

    private final Element serviceElement;
    private final String requestsServiceRoot;
    private Map<String, Integer> methodCount;

    public RequestFactorySourceWriter(Element serviceElement, ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
        this.serviceElement = serviceElement;
        this.requestsServiceRoot = serviceElement.getAnnotation(RequestFactory.class).serviceRoot();

        ObjectMapperProcessor.elementUtils = elements;
        ObjectMapperProcessor.typeUtils = types;
        ObjectMapperProcessor.messager = messager;
        ObjectMapperProcessor.filer = filer;
    }

    public RequestFactorySourceWriter(Element serviceElement, String serviceRoot, ProcessingEnvironment processingEnvironment) {
        super(processingEnvironment);
        this.serviceElement = serviceElement;
        this.requestsServiceRoot = serviceRoot;

        ObjectMapperProcessor.elementUtils = elements;
        ObjectMapperProcessor.typeUtils = types;
        ObjectMapperProcessor.messager = messager;
        ObjectMapperProcessor.filer = filer;
    }

    @Override
    public List<TypeSpec.Builder> asTypeBuilder() {
        String namePrefix = "";
        if (!ElementKind.PACKAGE.equals(serviceElement.getEnclosingElement().getKind())) {
            namePrefix = serviceElement.getEnclosingElement().getSimpleName().toString() + "_";
        }

        String factoryName = namePrefix+serviceElement.getSimpleName().toString() + "Factory";

        FieldSpec instanceField = FieldSpec.builder(ClassName.bestGuess(factoryName), "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new " + factoryName + "()")
                .build();

        methodCount = new HashMap<>();

        List<ProcessedType> processedTypes = new ArrayList<>();

        List<ServiceMethod> serviceMethods = getServiceMethods(processedTypes, "", serviceElement);

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

    public List<ServiceMethod> getServiceMethods(List<ProcessedType> processedTypes, String servicePath, Element serviceElement) {
        TypeElement serviceType = (TypeElement) serviceElement;
        String[] currentPath = new String[]{""};

        if (nonNull(serviceElement.getAnnotation(Path.class))) {
            String currentInterfacePath = serviceElement.getAnnotation(Path.class).value();
            currentPath[0] = servicePath.isEmpty() ? currentInterfacePath : (servicePath + pathsSplitter(servicePath, currentInterfacePath) + currentInterfacePath);
        } else {
            currentPath[0] = servicePath;
        }

        if (serviceType.getInterfaces().isEmpty()) {
            return getMethods(processedTypes, currentPath[0], serviceElement);
        }

        List<ServiceMethod> methods = new ArrayList<>();
        methods.addAll(getMethods(processedTypes, currentPath[0], serviceElement));
        ((TypeElement) serviceElement)
                .getInterfaces()
                .forEach(superInterface -> methods.addAll(getServiceMethods(processedTypes, currentPath[0], types.asElement(superInterface))));

        return methods;
    }

    private List<ServiceMethod> getMethods(List<ProcessedType> processedTypes, String servicePath, Element serviceElement) {
        ProcessedType processedType = new ProcessedType(elements, (TypeElement) serviceElement);
        processedTypes.add(processedType);
        return processorUtil.getElementMethods(serviceElement)
                .stream()
                .filter(executableElement -> notOverridden(executableElement, processedTypes))
                .map(executableElement -> {
                            processedType.addMethod(executableElement);
                            return asServiceMethod(servicePath, executableElement);
                        }
                )
                .collect(toList());
    }

    private boolean notOverridden(ExecutableElement method, List<ProcessedType> processedTypes) {
        for (ProcessedType processedType : processedTypes) {
            if (processedType.overrides(method)) {
                return false;
            }
        }
        return true;
    }

    private ServiceMethod asServiceMethod(String servicePath, ExecutableElement executableElement) {
        String name = executableElement.getSimpleName().toString();
        if (hasClassifier(executableElement)) {
            return new ServiceMethod(executableElement, 0, servicePath);
        } else {
            if (!methodCount.containsKey(name)) {
                methodCount.put(name, 1);
                return new ServiceMethod(executableElement, 0, servicePath);
            } else {
                Integer index = methodCount.get(name);
                methodCount.put(name, methodCount.get(name) + 1);
                return new ServiceMethod(executableElement, index, servicePath);
            }
        }
    }

    private MethodSpec makeRequestFactoryMethod(ServiceMethod serviceMethod) {
        String classifier = getMethodClassifier(serviceMethod);

        TypeName requestTypeName = TypeName.get(getRequestBeanType(serviceMethod).type);
        TypeMirror responseBean = getResponseBeanType(serviceMethod);

        MethodSpec.Builder request = MethodSpec.methodBuilder(serviceMethod.method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ServerRequest.class), requestTypeName, ClassName.get(responseBean)));

        getMethodParameters(serviceMethod)
                .forEach(parameter -> request.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString()));

        String requestClassName = serviceElement.getSimpleName().toString() + "_" + serviceMethod.method.getSimpleName() + classifier;
        String initializeStatement = requestClassName + " instance = new " + requestClassName;

        Optional<String> requestBodyParamName = getRequestBeanType(serviceMethod)
                .getParamName();

        if (requestBodyParamName.isPresent()) {
            request.addStatement(initializeStatement + "(" + requestBodyParamName.get() + ")");
        } else {
            request.addStatement(initializeStatement + "()");
        }

        getMethodParameters(serviceMethod)
                .stream()
                .filter(variableElement -> isCallArgument(variableElement))
                .forEach(parameter -> request.addStatement("instance.addCallArgument($S, $T.isNull($L)?\"\":$T.valueOf($L))", parameter.getSimpleName().toString(), Objects.class, parameter.getSimpleName().toString(), String.class, parameter.getSimpleName().toString()));

        serviceMethod.method.getParameters()
                .stream()
                .filter(parameter -> nonNull(parameter.getAnnotation(QueryParam.class)))
                .forEach(parameter -> request.addStatement("instance.setQueryParameter($S, $T.isNull($L)?\"\":$T.valueOf($L))",
                        parameter.getAnnotation(QueryParam.class).value(),
                        Objects.class,
                        parameter.getSimpleName(),
                        String.class,
                        parameter.getSimpleName()));

        serviceMethod.method.getParameters()
                .stream()
                .filter(parameter -> nonNull(parameter.getAnnotation(PathParam.class)))
                .forEach(parameter -> request.addStatement("instance.setPathParameter($S, $T.isNull($L)?\"\":$T.valueOf($L))",
                        parameter.getAnnotation(PathParam.class).value(),
                        Objects.class,
                        parameter.getSimpleName(),
                        String.class,
                        parameter.getSimpleName()));

        serviceMethod.method.getParameters()
                .stream()
                .filter(parameter -> nonNull(parameter.getAnnotation(HeaderParam.class)))
                .forEach(parameter -> request.addStatement("instance.setHeaderParameter($S, $T.isNull($L)?\"\":$T.valueOf($L))",
                        parameter.getAnnotation(HeaderParam.class).value(),
                        Objects.class,
                        parameter.getSimpleName(),
                        String.class,
                        parameter.getSimpleName()));

        request.addStatement("return instance");

        return request.build();
    }

    private boolean isCallArgument(VariableElement variableElement) {
        return isNull(variableElement.getAnnotation(QueryParam.class))
                && isNull(variableElement.getAnnotation(PathParam.class))
                && isNull(variableElement.getAnnotation(HeaderParam.class));
    }

    private List<VariableElement> getMethodParameters(ServiceMethod serviceMethod) {
        return serviceMethod.method
                .getParameters()
                .stream()
                .filter(this::isArgumentParam)
                .collect(toList());
    }

    private boolean isArgumentParam(VariableElement param) {
        return isNull(param.getAnnotation(Context.class))
                && isNull(param.getAnnotation(Suspended.class));
    }

    private TypeSpec makeRequestClass(ServiceMethod serviceMethod) {
        String classifier = getMethodClassifier(serviceMethod);
        TypeMirror requestType = getRequestBeanType(serviceMethod).type;
        TypeName requestTypeName = TypeName.get(requestType);
        TypeMirror responseBean = getResponseBeanType(serviceMethod);

        TypeSpec.Builder requestBuilder = TypeSpec
                .classBuilder(serviceElement.getSimpleName().toString() + "_" + serviceMethod.method.getSimpleName() + classifier)
                .addAnnotation(Request.class)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(ServerRequest.class),
                        requestTypeName,
                        ClassName.get(responseBean)))
                .addMethod(constructor(requestType, serviceMethod));

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

    private TypeMirror getResponseBeanType(ServiceMethod serviceMethod) {
        return getMappingType(serviceMethod.method.getReturnType());
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

    private RequestBodyParam getRequestBeanType(ServiceMethod serviceMethod) {
        List<? extends VariableElement> parameters = serviceMethod.method.getParameters();

        if(!isBodyHttpMethod(serviceMethod)){
            return RequestBodyParam.ofVoid(getMappingType(elements.getTypeElement(Void.class.getCanonicalName()).asType()));
        }

        List<VariableElement> qualified = parameters.stream()
                .filter(param -> isQualifiedForBody(param, serviceMethod))
                .collect(toList());

        if (qualified.isEmpty()) {
            return RequestBodyParam.ofVoid(getMappingType(elements.getTypeElement(Void.class.getCanonicalName()).asType()));
        }

        Optional<VariableElement> markedAsBody = qualified.stream()
                .filter(this::isBodyParameter)
                .findFirst();

        if (markedAsBody.isPresent()) {
            return new RequestBodyParam(markedAsBody.get(), getMappingType(markedAsBody.get().asType()));
        }

        VariableElement variableElement = qualified.get(qualified.size() - 1);
        return new RequestBodyParam(variableElement, getMappingType(variableElement.asType()));
    }

    private boolean isBodyHttpMethod(ServiceMethod serviceMethod) {
        return nonNull(serviceMethod.method.getAnnotation(POST.class))
                || nonNull(serviceMethod.method.getAnnotation(PUT.class))
                || nonNull(serviceMethod.method.getAnnotation(PATCH.class));
    }

    private boolean isQualifiedForBody(VariableElement param, ServiceMethod serviceMethod) {
        return isNull(param.getAnnotation(QueryParam.class))
                && isNull(param.getAnnotation(PathParam.class))
                && isNull(param.getAnnotation(HeaderParam.class))
                && isNull(param.getAnnotation(Context.class))
                && isNull(param.getAnnotation(Suspended.class))
                && notInPath(param, serviceMethod);
    }

    private boolean notInPath(VariableElement param, ServiceMethod serviceMethod) {
        String path = getPath(serviceMethod);
        String paramName = param.getSimpleName().toString();
        return !path.contains(":" + paramName + "/")
                && !path.contains("{" + paramName + "}")
                && !path.endsWith(":" + paramName);
    }

    private boolean isBodyParameter(VariableElement parameter) {
        return isRequestBean(parameter) || isRequestBody(parameter);
    }

    private boolean isRequestBody(VariableElement parameter) {
        TypeMirror typeMirror = parameter.asType();
        if (isPrimitive(typeMirror)
                || Type.isArray(typeMirror)
                || Type.is2dArray(typeMirror)
                || Type.isEnum(typeMirror)
                || Type.isCollection(typeMirror)
                || Type.isPrimitiveArray(typeMirror)
                || Type.isIterable(typeMirror)
                || Type.isMap(typeMirror)) {

            return nonNull(parameter.getAnnotation(RequestBody.class));
        } else {
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

    private MethodSpec constructor(TypeMirror requestBean, ServiceMethod serviceMethod) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

        boolean voidType = isVoidType(serviceMethod);
        if (!voidType) {
            constructorBuilder
                    .addParameter(TypeName.get(requestBean), "request");
        }

        constructorBuilder.addStatement("super(new $T($T.class, $S, $T.class, $T.class), $L)",
                RequestMeta.class,
                serviceElement.asType(),
                serviceMethod.method.getSimpleName().toString(),
                types.erasure(requestBean),
                types.erasure(serviceMethod.method.getReturnType()),
                voidType ? "null" : "request"
        );

        constructorBuilder.addStatement("setHttpMethod($S)", getHttpMethod(serviceMethod))
                .addStatement("setContentType(new String[]{$L})", getContentType(serviceMethod))
                .addStatement("setAccept(new String[]{$L})", getAcceptResponse(serviceMethod))
                .addStatement("setPath($S)", getPath(serviceMethod))
                .addStatement("setServiceRoot($S)", getServiceRoot(serviceMethod.method));

        Retries retries = serviceMethod.method.getAnnotation(Retries.class);
        if (nonNull(retries)) {
            constructorBuilder.addStatement("setTimeout($L)", retries.timeout());
            constructorBuilder.addStatement("setMaxRetries($L)", retries.maxRetries());
        }
        if (nonNull(serviceMethod.method.getAnnotation(SuccessCodes.class))) {
            constructorBuilder.addStatement("setSuccessCodes(new Integer[]{$L})", getSuccessCodes(serviceMethod));
        }

        if (!isVoidType(serviceMethod)) {
            Optional<CodeBlock> requestWriter = getRequestWriter(serviceMethod);
            requestWriter.ifPresent(constructorBuilder::addCode);
        }

        if (!isVoidType(serviceMethod.method.getReturnType())) {
            Optional<CodeBlock> responseReader = getResponseReader(serviceMethod);
            responseReader.ifPresent(constructorBuilder::addCode);
        }

        if (!isVoidType(serviceMethod)) {
            constructorBuilder.addCode(new ReplaceParametersMethodBuilder(messager, getPath(serviceMethod), serviceMethod.method).build());
        }

        return constructorBuilder.build();
    }

    private Optional<CodeBlock> getRequestWriter(ServiceMethod serviceMethod) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Writer annotation = serviceMethod.method.getAnnotation(Writer.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(serviceMethod.method, Writer.class, "value");
            value.ifPresent(writerType -> builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", TypeName.get(writerType)));
            return Optional.of(builder.build());
        } else if (consumesJson(serviceMethod)) {
            TypeMirror requestBeanType = getRequestBeanType(serviceMethod).type;
            boolean serializerGenerated = !shouldGenerateSerializer(requestBeanType);

            CodeBlock instance = new FieldSerializerChainBuilder(requestBeanType, serializerGenerated).getInstance(requestBeanType);
            TypeSpec.Builder writerType = TypeSpec.anonymousClassBuilder("$S", requestBeanType)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AbstractObjectWriter.class), TypeName.get(requestBeanType)))
                    .addMethod(MethodSpec.methodBuilder("newSerializer")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PROTECTED)
                            .returns(ParameterizedTypeName.get(ClassName.get(JsonSerializer.class), TypeVariableName.get("?")))
                            .addCode("return ")
                            .addCode(instance)
                            .addCode(";")
                            .build());

            builder.addStatement("setRequestWriter(bean -> $L.write(bean))", writerType.build());
            return Optional.of(builder.build());
        } else if (consumesText(serviceMethod)) {
            builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", StringWriter.class);
            return Optional.of(builder.build());
        }

        return Optional.empty();
    }

    private boolean consumesText(ServiceMethod serviceMethod) {
        String contentType = getContentType(serviceMethod);
        return contentType.contains(MediaType.TEXT_PLAIN);
    }

    private boolean consumesJson(ServiceMethod serviceMethod) {
        String contentType = getContentType(serviceMethod);
        return contentType.contains(MediaType.APPLICATION_JSON)
                || contentType.contains(MediaType.APPLICATION_JSON_PATCH_JSON);
    }

    private boolean shouldGenerateSerializer(TypeMirror requestBeanType) {
        return !(processorUtil.isPrimitive(requestBeanType)
                || processorUtil.isPrimitiveArray(requestBeanType)
                || processorUtil.isArray(requestBeanType)
                || processorUtil.is2dArray(requestBeanType)
                || processorUtil.isCollection(requestBeanType)
                || processorUtil.isIterable(requestBeanType)
                || processorUtil.isEnum(requestBeanType)
                || processorUtil.isMap(requestBeanType)
                || processorUtil.isStringType(requestBeanType)
                || isWrapperType(requestBeanType)
                || isSerializer(requestBeanType));

    }

    private boolean isSerializer(TypeMirror requestBeanType) {
        return nonNull(requestBeanType.getAnnotation(JSONMapper.class))
                || nonNull(requestBeanType.getAnnotation(JSONWriter.class));
    }

    private boolean shouldGenerateDeserializer(TypeMirror requestBeanType) {
        return !(processorUtil.isPrimitive(requestBeanType)
                || processorUtil.isPrimitiveArray(requestBeanType)
                || processorUtil.isArray(requestBeanType)
                || processorUtil.is2dArray(requestBeanType)
                || processorUtil.isCollection(requestBeanType)
                || processorUtil.isIterable(requestBeanType)
                || processorUtil.isEnum(requestBeanType)
                || processorUtil.isMap(requestBeanType)
                || processorUtil.isStringType(requestBeanType)
                || isWrapperType(requestBeanType)
                || isDeserializer(requestBeanType));

    }

    private boolean isDeserializer(TypeMirror requestBeanType) {
        return nonNull(requestBeanType.getAnnotation(JSONMapper.class))
                || nonNull(requestBeanType.getAnnotation(JSONReader.class));
    }

    private Optional<CodeBlock> getResponseReader(ServiceMethod serviceMethod) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Reader annotation = serviceMethod.method.getAnnotation(Reader.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(serviceMethod.method, Reader.class, "value");
            value.ifPresent(readerType -> builder.addStatement("setResponseReader(response -> new $T().read(response))", TypeName.get(readerType)));
            return Optional.of(builder.build());
        } else if (producesJson(serviceMethod)) {

            TypeMirror responseBeanType = getResponseBeanType(serviceMethod);
            boolean deserializerGenerated = !shouldGenerateDeserializer(responseBeanType);
            CodeBlock instance = new FieldDeserializersChainBuilder(responseBeanType, deserializerGenerated).getInstance(responseBeanType);
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

            builder.addStatement("setResponseReader(response -> $L.read(response.getBodyAsString()))", readerType.build());
            return Optional.of(builder.build());
        } else if (producesText(serviceMethod)) {
            builder.addStatement("setResponseReader(response -> new $T().read(response))", TypeName.get(StringReader.class));
            return Optional.of(builder.build());
        }

        return Optional.empty();
    }

    private boolean producesJson(ServiceMethod serviceMethod) {
        String acceptResponse = getAcceptResponse(serviceMethod);
        return acceptResponse.contains(MediaType.APPLICATION_JSON)
                || acceptResponse.contains(MediaType.APPLICATION_JSON_PATCH_JSON);
    }

    private boolean producesText(ServiceMethod serviceMethod) {
        String acceptResponse = getAcceptResponse(serviceMethod);
        return acceptResponse.contains(MediaType.TEXT_PLAIN);
    }

    private boolean isVoidType(ServiceMethod serviceMethod) {
        return getRequestBeanType(serviceMethod)._void;
    }

    private boolean isVoidType(TypeMirror type) {
        return TypeKind.VOID.equals(type.getKind()) || types.isSameType(elements.getTypeElement(Void.class.getCanonicalName()).asType(), type);
    }

    private String getContentType(ServiceMethod serviceMethod) {
        if (nonNull(serviceMethod.method.getAnnotation(Consumes.class))) {
            return Arrays.stream(serviceMethod.method.getAnnotation(Consumes.class).value()).map(s -> "\"" + s + "\"")
                    .collect(joining(","));
        } else {
            return "\"" + MediaType.APPLICATION_JSON + "\"";
        }
    }

    private String getAcceptResponse(ServiceMethod serviceMethod) {
        if (nonNull(serviceMethod.method.getAnnotation(Produces.class))) {
            return Arrays.stream(serviceMethod.method.getAnnotation(Produces.class).value()).map(s -> "\"" + s + "\"")
                    .collect(joining(","));
        } else {
            return "\"" + MediaType.APPLICATION_JSON + "\"";
        }
    }

    private String getPath(ServiceMethod serviceMethod) {
        Path pathAnnotation = serviceMethod.method.getAnnotation(Path.class);
        String methodPath = isNull(pathAnnotation) ? "" : pathAnnotation.value();
        return serviceMethod.servicePath + pathsSplitter(serviceMethod.servicePath, methodPath) + methodPath;
    }

    private String pathsSplitter(String servicePath, String methodPath) {
        return (servicePath.endsWith("/") || methodPath.startsWith("/")) ? "" : "/";
    }

    private String getServiceRoot(ExecutableElement method) {
        ServiceRoot serviceRoot = method.getAnnotation(ServiceRoot.class);
        if (nonNull(serviceRoot)) {
            return serviceRoot.value();
        } else {
            return requestsServiceRoot;
        }
    }

    private String getSuccessCodes(ServiceMethod serviceMethod) {
        return IntStream.of(serviceMethod.method.getAnnotation(SuccessCodes.class).value())
                .boxed()
                .map(String::valueOf)
                .collect(joining(","));
    }

    private String getHttpMethod(ServiceMethod serviceMethod) {

        ExecutableElement method = serviceMethod.method;
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

    /**
     * <p>wrapperType.</p>
     *
     * @param type a {@link TypeMirror} object.
     * @return a {@link TypeName} object.
     */
    public boolean isWrapperType(TypeMirror type) {
        return processorUtil.isAssignableFrom(type, Byte.class)
                || processorUtil.isAssignableFrom(type, Short.class)
                || processorUtil.isAssignableFrom(type, Integer.class)
                || processorUtil.isAssignableFrom(type, Long.class)
                || processorUtil.isAssignableFrom(type, Float.class)
                || processorUtil.isAssignableFrom(type, Double.class)
                || processorUtil.isAssignableFrom(type, Date.class)
                || processorUtil.isAssignableFrom(type, BigDecimal.class)
                || processorUtil.isAssignableFrom(type, Character.class)
                || processorUtil.isAssignableFrom(type, Boolean.class);
    }

    private static class ServiceMethod {
        private ExecutableElement method;
        private Integer index;
        private String servicePath;

        public ServiceMethod(ExecutableElement method, Integer index, String servicePath) {
            this.method = method;
            this.index = index;
            this.servicePath = servicePath;
        }
    }

    private static class RequestBodyParam {
        private boolean _void;
        private VariableElement param;
        private TypeMirror type;

        public RequestBodyParam(VariableElement param, TypeMirror type) {
            this._void = false;
            this.param = param;
            this.type = type;
        }

        public static RequestBodyParam ofVoid(TypeMirror typeMirror) {
            RequestBodyParam requestBodyParam = new RequestBodyParam(null, typeMirror);
            requestBodyParam._void = true;
            return requestBodyParam;
        }

        public Optional<String> getParamName() {
            if (!_void) {
                return Optional.of(param.getSimpleName().toString());
            }
            return Optional.empty();
        }
    }

}

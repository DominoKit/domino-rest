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

    @Override
    public List<TypeSpec.Builder> asTypeBuilder() {
        String factoryName = serviceElement.getSimpleName().toString() + "Factory";

        FieldSpec instanceField = FieldSpec.builder(ClassName.bestGuess(factoryName), "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new " + factoryName + "()")
                .build();

        methodCount = new HashMap<>();

        List<ServiceMethod> serviceMethods = getServiceMethods("", serviceElement);

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

    /*

    getServiceMethods(interface, parentPath)
    {
        parent interfaces -> getServiceMethods(parentInterface, null)
        ServiceMethod -> parentPath + interface path + method path
    }

     */

    public List<ServiceMethod> getServiceMethods(String servicePath, Element serviceElement) {
        TypeElement serviceType = (TypeElement) serviceElement;
        String[] currentPath = new String[]{""};
        if (nonNull(serviceElement.getAnnotation(Path.class))) {
            String currentInterfacePath = serviceElement.getAnnotation(Path.class).value();
            currentPath[0] = servicePath.isEmpty() ? currentInterfacePath : (servicePath + pathsSplitter(servicePath, currentInterfacePath) + currentInterfacePath);
        } else {
            currentPath[0] = servicePath;
        }
        if (serviceType.getInterfaces().isEmpty()) {
            return getMethods(currentPath[0], serviceElement);
        }

        List<ServiceMethod> methods = new ArrayList<>();
        methods.addAll(getMethods(currentPath[0], serviceElement));
        ((TypeElement) serviceElement)
                .getInterfaces()
                .forEach(superInterface -> methods.addAll(getServiceMethods(currentPath[0], types.asElement(superInterface))));

        return methods;
    }

    private List<ServiceMethod> getMethods(String servicePath, Element serviceElement) {
        return processorUtil.getElementMethods(serviceElement)
                .stream()
                .map(executableElement ->
                        asServiceMethod(servicePath, executableElement)
                ).collect(toList());
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

        TypeName requestTypeName = TypeName.get(getRequestBeanType(serviceMethod));
        TypeMirror responseBean = getResponseBeanType(serviceMethod);

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
        } else {
            request.addStatement(initializeStatement + "()");
        }

        serviceMethod.method.getParameters()
                .forEach(parameter -> request.addStatement("instance.addCallArgument($S, $T.isNull($L)?\"\":$T.valueOf($L))", parameter.getSimpleName().toString(), Objects.class, parameter.getSimpleName().toString(), String.class, parameter.getSimpleName().toString()));

        serviceMethod.method.getParameters()
                .stream()
                .filter(parameter -> nonNull(parameter.getAnnotation(QueryParam.class)))
                .forEach(parameter -> request.addStatement("instance.setParameter($S, $T.isNull($L)?\"\":$T.valueOf($L))",
                        parameter.getAnnotation(QueryParam.class).value(),
                        Objects.class,
                        parameter.getSimpleName(),
                        String.class,
                        parameter.getSimpleName()));

        request.addStatement("return instance");

        return request.build();
    }

    private TypeSpec makeRequestClass(ServiceMethod serviceMethod) {
        String classifier = getMethodClassifier(serviceMethod);
        TypeMirror requestType = getRequestBeanType(serviceMethod);
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

    private TypeMirror getRequestBeanType(ServiceMethod serviceMethod) {
        List<? extends VariableElement> parameters = serviceMethod.method.getParameters();

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
        if (isVoidType(requestBean)) {
            constructorBuilder.addStatement("super(null)");
        } else {
            constructorBuilder
                    .addParameter(TypeName.get(requestBean), "request")
                    .addStatement("super(request)");
        }

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
            constructorBuilder.addCode(getRequestWriter(serviceMethod));
        }

        if (!isVoidType(serviceMethod.method.getReturnType())) {
            constructorBuilder.addCode(getResponseReader(serviceMethod));
        }

        if (!isVoidType(serviceMethod)) {
            constructorBuilder.addCode(new ReplaceParametersMethodBuilder(messager, getPath(serviceMethod), serviceMethod.method).build());
        }

        return constructorBuilder.build();
    }

    private CodeBlock getRequestWriter(ServiceMethod serviceMethod) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Writer annotation = serviceMethod.method.getAnnotation(Writer.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(serviceMethod.method, Writer.class, "value");
            value.ifPresent(writerType -> builder.addStatement("setRequestWriter(bean -> new $T().write(bean))", TypeName.get(writerType)));
        } else {
            TypeMirror requestBeanType = getRequestBeanType(serviceMethod);
            CodeBlock instance = new FieldSerializerChainBuilder(requestBeanType).getInstance(requestBeanType);
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
        }

        return builder.build();
    }

    private CodeBlock getResponseReader(ServiceMethod serviceMethod) {
        CodeBlock.Builder builder = CodeBlock.builder();

        Reader annotation = serviceMethod.method.getAnnotation(Reader.class);
        if (nonNull(annotation)) {
            Optional<TypeMirror> value = processorUtil.getClassValueFromAnnotation(serviceMethod.method, Reader.class, "value");
            value.ifPresent(readerType -> builder.addStatement("setResponseReader(response -> new $T().read(response))", TypeName.get(readerType)));
        } else {

            TypeMirror responseBeanType = getResponseBeanType(serviceMethod);
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

            builder.addStatement("setResponseReader(response -> $L.read(response.getBodyAsString()))", readerType.build());
        }

        return builder.build();
    }

    private boolean isVoidType(ServiceMethod serviceMethod) {
        return isVoidType(getRequestBeanType(serviceMethod));
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
        String methodPath = serviceMethod.method.getAnnotation(Path.class).value();
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

}

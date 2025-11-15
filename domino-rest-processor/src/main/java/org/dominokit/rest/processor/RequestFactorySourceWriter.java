/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.processor;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import dominojackson.shaded.com.squareup.javapoet.*;
import dominojackson.shaded.org.dominokit.domino.apt.commons.AbstractSourceBuilder;
import dominojackson.shaded.org.dominokit.domino.apt.commons.DominoTypeBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.dominokit.jackson.AbstractObjectReader;
import org.dominokit.jackson.AbstractObjectWriter;
import org.dominokit.jackson.JsonDeserializer;
import org.dominokit.jackson.JsonSerializer;
import org.dominokit.jackson.annotation.JSONMapper;
import org.dominokit.jackson.annotation.JSONReader;
import org.dominokit.jackson.annotation.JSONWriter;
import org.dominokit.jackson.processor.ObjectMapperProcessor;
import org.dominokit.jackson.processor.Type;
import org.dominokit.jackson.processor.deserialization.FieldDeserializersChainBuilder;
import org.dominokit.jackson.processor.serialization.FieldSerializerChainBuilder;
import org.dominokit.rest.shared.MultipartForm;
import org.dominokit.rest.shared.Response;
import org.dominokit.rest.shared.request.*;
import org.dominokit.rest.shared.request.service.annotations.*;
import org.dominokit.rest.shared.request.service.annotations.Request;

/** Writes the factory generated class */
public class RequestFactorySourceWriter extends AbstractSourceBuilder {

  private final Element serviceElement;
  private final String requestsServiceRoot;
  private Map<String, Integer> methodCount;

  public RequestFactorySourceWriter(
      Element serviceElement, ProcessingEnvironment processingEnvironment) {
    super(processingEnvironment);
    this.serviceElement = serviceElement;
    this.requestsServiceRoot = serviceElement.getAnnotation(RequestFactory.class).serviceRoot();

    ObjectMapperProcessor.elementUtils = elements;
    ObjectMapperProcessor.typeUtils = types;
    ObjectMapperProcessor.messager = messager;
    ObjectMapperProcessor.filer = filer;
  }

  public RequestFactorySourceWriter(
      Element serviceElement, String serviceRoot, ProcessingEnvironment processingEnvironment) {
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

    String factoryName = namePrefix + serviceElement.getSimpleName().toString() + "Factory";

    FieldSpec instanceField =
        FieldSpec.builder(
                ClassName.bestGuess(factoryName),
                "INSTANCE",
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL)
            .initializer("new " + factoryName + "()")
            .build();

    methodCount = new HashMap<>();

    List<ProcessedType> processedTypes = new ArrayList<>();

    List<ServiceMethod> serviceMethods = getServiceMethods(processedTypes, "", serviceElement);

    List<TypeSpec> requests = serviceMethods.stream().map(this::makeRequestClass).collect(toList());

    List<MethodSpec> overrideMethods =
        serviceMethods.stream().map(this::makeRequestFactoryMethod).collect(toList());

    TypeSpec.Builder factory =
        DominoTypeBuilder.classBuilder(factoryName, RequestFactoryProcessor.class)
            .addAnnotation(
                AnnotationSpec.builder(RestService.class)
                    .addMember("value", "$T.class", serviceElement.asType())
                    .build())
            .addField(instanceField)
            .addTypes(requests)
            .addMethods(overrideMethods);

    return Collections.singletonList(factory);
  }

  private List<ServiceMethod> getServiceMethods(
      List<ProcessedType> processedTypes, String servicePath, Element serviceElement) {
    TypeElement serviceType = (TypeElement) serviceElement;
    String[] currentPath = new String[] {""};

    if (nonNull(serviceElement.getAnnotation(Path.class))) {
      String currentInterfacePath = serviceElement.getAnnotation(Path.class).value();
      currentPath[0] =
          servicePath.isEmpty()
              ? currentInterfacePath
              : (servicePath
                  + pathsSplitter(servicePath, currentInterfacePath)
                  + currentInterfacePath);
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
        .forEach(
            superInterface ->
                methods.addAll(
                    getServiceMethods(
                        processedTypes, currentPath[0], types.asElement(superInterface))));

    return methods;
  }

  private List<ServiceMethod> getMethods(
      List<ProcessedType> processedTypes, String servicePath, Element serviceElement) {
    ProcessedType processedType = new ProcessedType(elements, (TypeElement) serviceElement);
    processedTypes.add(processedType);
    return processorUtil.getElementMethods(serviceElement).stream()
        .filter(executableElement -> notOverridden(executableElement, processedTypes))
        .map(
            executableElement -> {
              processedType.addMethod(executableElement);
              return asServiceMethod(servicePath, executableElement);
            })
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

    MethodSpec.Builder request =
        MethodSpec.methodBuilder(serviceMethod.method.getSimpleName().toString())
            .addModifiers(Modifier.PUBLIC)
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(ServerRequest.class),
                    requestTypeName,
                    ClassName.get(responseBean)));

    getMethodParameters(serviceMethod)
        .forEach(
            parameter ->
                request.addParameter(
                    getParameterType(parameter), parameter.getSimpleName().toString()));

    String requestClassName =
        serviceElement.getSimpleName().toString()
            + "_"
            + serviceMethod.method.getSimpleName()
            + classifier;
    String initializeStatement = requestClassName + " instance = new " + requestClassName;

    Optional<String> requestBodyParamName = getRequestBeanType(serviceMethod).getParamName();

    if (consumesMultipartForm(serviceMethod)) {
      request.addStatement("MultipartForm multipartForm = new MultipartForm()");
      getMethodParameters(serviceMethod)
          .forEach(
              variableElement -> {
                if (variableElement.getAnnotation(Multipart.class) != null) {
                  // get from object
                  getFromModel(serviceMethod, request, variableElement);
                } else {
                  if (!isParamField(variableElement)) {
                    addMultipart(
                        serviceMethod,
                        request,
                        variableElement,
                        variableElement.getSimpleName().toString());
                  }
                }
              });
      request.addStatement(initializeStatement + "(multipartForm)");
    } else if (requestBodyParamName.isPresent()) {
      request.addStatement(initializeStatement + "(" + requestBodyParamName.get() + ")");
    } else {
      request.addStatement(initializeStatement + "()");
    }

    serviceMethod.method.getParameters().stream()
        .filter(parameter -> nonNull(parameter.getAnnotation(QueryParam.class)))
        .forEach(parameter -> addSetParameterStatement(request, parameter));

    serviceMethod.method.getParameters().stream()
        .filter(parameter -> nonNull(parameter.getAnnotation(MatrixParam.class)))
        .forEach(parameter -> addSetMatrixParameterStatement(request, parameter));

    serviceMethod.method.getParameters().stream()
        .filter(parameter -> nonNull(parameter.getAnnotation(PathParam.class)))
        .forEach(
            parameter -> {
              if (processorUtil.isAssignableFrom(parameter, Date.class)
                  && nonNull(parameter.getAnnotation(DateFormat.class))) {
                request.addStatement(
                    "$T.setPathParameter(instance, $S, () -> $L, $S)",
                    TypeName.get(ParameterSetter.class),
                    parameter.getAnnotation(PathParam.class).value(),
                    parameter.getSimpleName(),
                    parameter.getAnnotation(DateFormat.class).value());
              } else {
                request.addStatement(
                    "$T.setPathParameter(instance, $S, () -> $L)",
                    TypeName.get(ParameterSetter.class),
                    parameter.getAnnotation(PathParam.class).value(),
                    parameter.getSimpleName());
              }
            });

    serviceMethod.method.getParameters().stream()
        .filter(parameter -> nonNull(parameter.getAnnotation(HeaderParam.class)))
        .forEach(
            parameter -> {
              if (processorUtil.isAssignableFrom(parameter, Date.class)
                  && nonNull(parameter.getAnnotation(DateFormat.class))) {
                request.addStatement(
                    "instance.setPathParameter($S, instance.formatDate(() -> $L, $S))",
                    parameter.getAnnotation(HeaderParam.class).value(),
                    parameter.getSimpleName(),
                    parameter.getAnnotation(DateFormat.class).value());
              } else {
                request.addStatement(
                    "instance.setHeaderParameter($S, instance.emptyOrStringValue(() -> $L))",
                    parameter.getAnnotation(HeaderParam.class).value(),
                    parameter.getSimpleName());
              }
            });

    serviceMethod.method.getParameters().stream()
        .filter(parameter -> nonNull(parameter.getAnnotation(BeanParam.class)))
        .forEach(
            parameter -> {
              CodeBlock.Builder builder = CodeBlock.builder();
              getBeanParams(builder, parameter);
              request.addCode(builder.build());
            });

    request.addStatement("return instance");

    return request.build();
  }

  private void getFromModel(
      ServiceMethod serviceMethod, MethodSpec.Builder request, VariableElement variableElement) {
    Element multipartElement = types.asElement(variableElement.asType());
    multipartElement.getEnclosedElements().stream()
        .filter(element -> element.getKind() == ElementKind.FIELD)
        .filter(element -> element.getAnnotation(FormParam.class) != null)
        .forEach(
            field -> {
              addMultipart(
                  serviceMethod,
                  request,
                  (VariableElement) field,
                  variableElement.getSimpleName().toString()
                      + "."
                      + fieldOrGetter(multipartElement, field));
            });
  }

  private TypeName getParameterType(VariableElement parameter) {
    if (Type.isPrimitiveArray(parameter.asType())
        && Type.arrayComponentType(parameter.asType()).getKind() == TypeKind.BYTE) {
      return TypeName.get(ByteArrayProvider.class);
    }
    return TypeName.get(parameter.asType());
  }

  private void addMultipart(
      ServiceMethod serviceMethod,
      MethodSpec.Builder request,
      VariableElement variableElement,
      String paramName) {
    FormParam annotation = variableElement.getAnnotation(FormParam.class);
    Optional<FileName> fileName =
        Optional.ofNullable(variableElement.getAnnotation(FileName.class));
    if (nonNull(annotation)) {
      if (isRequestBody(variableElement)) {
        Optional<CodeBlock> requestWriter =
            getRequestWriter(serviceMethod.method, variableElement.asType(), serviceMethod);
        requestWriter.ifPresent(
            codeBlock -> {
              if (fileName.isPresent()) {
                request.addStatement(
                    "multipartForm.append($S, () -> $L.write($L), $S, $S)",
                    annotation.value(),
                    codeBlock,
                    paramName,
                    MediaType.APPLICATION_JSON,
                    fileName.get().value());
              } else {
                request.addStatement(
                    "multipartForm.append($S, () -> $L.write($L), $S)",
                    annotation.value(),
                    codeBlock,
                    paramName,
                    MediaType.APPLICATION_JSON);
              }
            });
      } else if (Type.isPrimitiveArray(variableElement.asType())
          && Type.arrayComponentType(variableElement.asType()).getKind() == TypeKind.BYTE) {
        if (variableElement.getKind() == ElementKind.FIELD) {
          if (fileName.isPresent()) {
            request.addStatement(
                "multipartForm.append($S, $L, $S, $S)",
                annotation.value(),
                paramName,
                MediaType.APPLICATION_OCTET_STREAM,
                fileName.get().value());
          } else {
            request.addStatement(
                "multipartForm.append($S, $L, $S)",
                annotation.value(),
                paramName,
                MediaType.APPLICATION_OCTET_STREAM);
          }
        } else {
          if (fileName.isPresent()) {
            request.addStatement(
                "multipartForm.append($S, $L.getData(), $S, $S)",
                annotation.value(),
                paramName,
                MediaType.APPLICATION_OCTET_STREAM,
                fileName.get().value());
          } else {
            request.addStatement(
                "multipartForm.append($S, $L.getData(), $S, $L.getFileName())",
                annotation.value(),
                paramName,
                MediaType.APPLICATION_OCTET_STREAM,
                paramName);
          }
        }

      } else {
        if (processorUtil.isStringType(variableElement.asType())) {
          if (fileName.isPresent()) {
            request.addStatement(
                "multipartForm.append($S, () -> $L, $S, $S)",
                annotation.value(),
                paramName,
                MediaType.TEXT_PLAIN,
                fileName.get().value());
          } else {
            request.addStatement(
                "multipartForm.append($S, () -> $L, $S)",
                annotation.value(),
                paramName,
                MediaType.TEXT_PLAIN);
          }
        } else if (processorUtil.isPrimitive(variableElement.asType())) {
          if (fileName.isPresent()) {
            request.addStatement(
                "multipartForm.append($S, () -> $T.valueOf($L), $S, $S)",
                annotation.value(),
                String.class,
                paramName,
                MediaType.TEXT_PLAIN,
                fileName.get().value());
          } else {
            request.addStatement(
                "multipartForm.append($S, () -> $T.valueOf($L), $S)",
                annotation.value(),
                String.class,
                paramName,
                MediaType.TEXT_PLAIN);
          }
        } else {
          if (fileName.isPresent()) {
            request.addStatement(
                "multipartForm.append($S, () -> $L.toString(), $S, $S)",
                annotation.value(),
                paramName,
                MediaType.TEXT_PLAIN,
                fileName.get().value());
          } else {
            request.addStatement(
                "multipartForm.append($S, () -> $L.toString(), $S)",
                annotation.value(),
                paramName,
                MediaType.TEXT_PLAIN);
          }
        }
      }
    }
  }

  private void addSetParameterStatement(MethodSpec.Builder request, VariableElement parameter) {
    addSetParameterStatement(
        request, parameter, parameter.getAnnotation(QueryParam.class).value(), "Query");
  }

  private void addSetMatrixParameterStatement(
      MethodSpec.Builder request, VariableElement parameter) {
    addSetParameterStatement(
        request, parameter, parameter.getAnnotation(MatrixParam.class).value(), "Matrix");
  }

  /**
   * Shared implementation for Query / Matrix parameters.
   *
   * @param request the method builder
   * @param parameter the parameter element
   * @param paramName the name used in @QueryParam/@MatrixParam
   * @param type "Query" or "Matrix"
   */
  private void addSetParameterStatement(
      MethodSpec.Builder request, VariableElement parameter, String paramName, String type) {

    DateFormat dateFormat = parameter.getAnnotation(DateFormat.class);

    if (processorUtil.isCollection(parameter.asType())) {
      TypeMirror typeArgument = processorUtil.firstTypeArgument(parameter.asType());
      if (processorUtil.isAssignableFrom(typeArgument, Date.class) && nonNull(dateFormat)) {
        request.addStatement(
            "$T.setDateCollectionParameter(instance, $S, () -> $L, $S, $S)",
            TypeName.get(ParameterSetter.class),
            paramName,
            parameter.getSimpleName(),
            dateFormat.value(),
            type);
      } else {
        request.addStatement(
            "$T.setCollectionParameter(instance, $S, () -> $L, $S)",
            TypeName.get(ParameterSetter.class),
            paramName,
            parameter.getSimpleName(),
            type);
      }
    } else if (processorUtil.isAssignableFrom(parameter, Date.class) && nonNull(dateFormat)) {
      request.addStatement(
          "$T.setDateParameter(instance, $S, () -> $L, $S, $S)",
          TypeName.get(ParameterSetter.class),
          paramName,
          parameter.getSimpleName(),
          dateFormat.value(),
          type);
    } else {
      request.addStatement(
          "$T.setParameter(instance, $S, () -> $L, $S)",
          TypeName.get(ParameterSetter.class),
          paramName,
          parameter.getSimpleName(),
          type);
    }
  }

  private void getBeanParams(CodeBlock.Builder codeBlock, VariableElement parameter) {

    TypeMirror beanType = parameter.asType();

    Element beanElement = types.asElement(beanType);

    getBeanParams(codeBlock, beanElement, parameter.getSimpleName().toString());
  }

  private void getBeanParams(
      CodeBlock.Builder codeBlock, Element beanElement, String getterPrefixLiteral) {
    beanElement.getEnclosedElements().stream()
        .filter(element -> ElementKind.FIELD.equals(element.getKind()))
        .forEach(
            field -> {
              if (isParamField(field)) {
                if (nonNull(field.getAnnotation(PathParam.class))) {
                  PathParam pathParam = field.getAnnotation(PathParam.class);
                  if (processorUtil.isAssignableFrom(field, Date.class)
                      && nonNull(field.getAnnotation(DateFormat.class))) {
                    codeBlock.addStatement(
                        "instance.setPathParameter($S, instance.formatDate(() -> $L.$L, $S))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field),
                        field.getAnnotation(DateFormat.class).value());
                  } else {
                    codeBlock.addStatement(
                        "instance.setPathParameter($S, instance.emptyOrStringValue(() -> $L.$L))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field));
                  }
                }

                if (nonNull(field.getAnnotation(QueryParam.class))) {
                  QueryParam pathParam = field.getAnnotation(QueryParam.class);
                  if (processorUtil.isAssignableFrom(field, Date.class)
                      && nonNull(field.getAnnotation(DateFormat.class))) {
                    codeBlock.addStatement(
                        "instance.setQueryParameter($S, instance.formatDate(() -> $L.$L, $S))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field),
                        field.getAnnotation(DateFormat.class).value());
                  } else {
                    codeBlock.addStatement(
                        "instance.setQueryParameter($S, instance.emptyOrStringValue(() -> $L.$L))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field));
                  }
                }

                if (nonNull(field.getAnnotation(HeaderParam.class))) {
                  HeaderParam pathParam = field.getAnnotation(HeaderParam.class);
                  if (processorUtil.isAssignableFrom(field, Date.class)
                      && nonNull(field.getAnnotation(DateFormat.class))) {
                    codeBlock.addStatement(
                        "instance.setHeaderParameter($S, instance.formatDate(() -> $L.$L, $S))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field),
                        field.getAnnotation(DateFormat.class).value());
                  } else {
                    codeBlock.addStatement(
                        "instance.setHeaderParameter($S, instance.emptyOrStringValue(() -> $L.$L))",
                        pathParam.value(),
                        getterPrefixLiteral,
                        fieldOrGetter(beanElement, field));
                  }
                }
              } else {
                if (couldHaveNestedParams(field)) {
                  getBeanParams(
                      codeBlock,
                      types.asElement(field.asType()),
                      getterPrefixLiteral + "." + fieldOrGetter(beanElement, field));
                }
              }
            });
  }

  private boolean couldHaveNestedParams(Element field) {
    return !(isPrimitive(field.asType())
        || isWrapperType(field.asType())
        || processorUtil.isEnum(field.asType())
        || processorUtil.isArray(field.asType())
        || processorUtil.is2dArray(field.asType())
        || processorUtil.isCollection(field.asType())
        || processorUtil.isIterable(field.asType())
        || processorUtil.isMap(field.asType())
        || processorUtil.isStringType(field.asType())
        || processorUtil.isPrimitiveArray(field.asType()));
  }

  private boolean isParamField(Element field) {
    return nonNull(field.getAnnotation(PathParam.class))
        || nonNull(field.getAnnotation(QueryParam.class))
        || nonNull(field.getAnnotation(HeaderParam.class));
  }

  private String fieldOrGetter(Element beanElement, Element field) {
    BeanAccessors beanAccessors = new BeanAccessors(processorUtil, beanElement);
    AccessorInfo accessorInfo = beanAccessors.getterInfo(field);
    if (accessorInfo.getMethod().isPresent()) {
      return accessorInfo.getName() + "()";
    }
    return accessorInfo.getName();
  }

  private List<VariableElement> getMethodParameters(ServiceMethod serviceMethod) {
    return serviceMethod.method.getParameters().stream()
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

    TypeSpec.Builder requestBuilder =
        TypeSpec.classBuilder(
                serviceElement.getSimpleName().toString()
                    + "_"
                    + serviceMethod.method.getSimpleName()
                    + classifier)
            .addAnnotation(Request.class)
            .addModifiers(Modifier.PUBLIC)
            .superclass(
                ParameterizedTypeName.get(
                    ClassName.get(ServerRequest.class),
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

    if (processorUtil.isAssignableFrom(returnType, jakarta.ws.rs.core.Response.class)) {
      return elements.getTypeElement(Response.class.getCanonicalName()).asType();
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
    if (consumesMultipartForm(serviceMethod)) {
      return new RequestBodyParam(
          null,
          getMappingType(elements.getTypeElement(MultipartForm.class.getCanonicalName()).asType()));
    }
    List<? extends VariableElement> parameters = serviceMethod.method.getParameters();

    if (!isBodyHttpMethod(serviceMethod)) {
      return RequestBodyParam.ofVoid(
          getMappingType(elements.getTypeElement(Void.class.getCanonicalName()).asType()));
    }

    List<VariableElement> qualified =
        parameters.stream()
            .filter(param -> isQualifiedForBody(param, serviceMethod))
            .collect(toList());

    if (qualified.isEmpty()) {
      return RequestBodyParam.ofVoid(
          getMappingType(elements.getTypeElement(Void.class.getCanonicalName()).asType()));
    }

    Optional<VariableElement> markedAsBody =
        qualified.stream().filter(this::isBodyParameter).findFirst();

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
      Element element = types.asElement(typeMirror);
      RequestBody pojoAnnotation = parameter.getAnnotation(RequestBody.class);
      JSONMapper mapperAnnotation = element.getAnnotation(JSONMapper.class);
      return nonNull(parameter.getAnnotation(RequestBody.class))
          || nonNull(pojoAnnotation)
          || nonNull(mapperAnnotation);
    }
  }

  private boolean isRequestBean(VariableElement parameter) {
    return processorUtil.isAssignableFrom(parameter, RequestBean.class);
  }

  private MethodSpec constructor(TypeMirror requestBean, ServiceMethod serviceMethod) {
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

    boolean voidType = isVoidType(serviceMethod);
    if (!voidType) {
      constructorBuilder.addParameter(TypeName.get(requestBean), "request");
    }

    constructorBuilder.addStatement(
        "super(new $T($T.class, $S, $T.class, $T.class), $L)",
        RequestMeta.class,
        serviceElement.asType(),
        serviceMethod.method.getSimpleName().toString(),
        types.erasure(requestBean),
        types.erasure(serviceMethod.method.getReturnType()),
        voidType ? "null" : "request");

    constructorBuilder
        .addStatement("setHttpMethod($S)", getHttpMethod(serviceMethod))
        .addStatement("setAccept(new String[]{$L})", getAcceptResponse(serviceMethod))
        .addStatement("setPath($S)", getPath(serviceMethod))
        .addStatement("setServiceRoot($S)", getServiceRoot(serviceMethod.method));

    if (!consumesMultipartForm(serviceMethod)) {
      constructorBuilder.addStatement(
          "setContentType(new String[]{$L})", getContentType(serviceMethod));
    }

    Retries retries = serviceMethod.method.getAnnotation(Retries.class);
    if (nonNull(retries)) {
      constructorBuilder.addStatement("setTimeout($L)", retries.timeout());
      constructorBuilder.addStatement("setMaxRetries($L)", retries.maxRetries());
    }

    WithCredentials withCredentials = serviceMethod.method.getAnnotation(WithCredentials.class);
    if (nonNull(withCredentials)) {
      constructorBuilder.addStatement("setWithCredentials($L)", withCredentials.value());
    }

    if (nonNull(serviceMethod.method.getAnnotation(SuccessCodes.class))) {
      constructorBuilder.addStatement(
          "setSuccessCodes(new Integer[]{$L})", getSuccessCodes(serviceMethod));
    }

    Optional<NullQueryParamStrategy> nullQueryParamStrategy =
        getNullQueryParamStrategy(requestBean, serviceMethod);

    nullQueryParamStrategy.ifPresent(
        strategy ->
            constructorBuilder.addStatement(
                "setNullQueryParamStrategy($T.$L)", NullQueryParamStrategy.class, strategy));

    if (consumesMultipartForm(serviceMethod)) {
      constructorBuilder.addStatement("setMultipartForm(true)");
    } else if (!isVoidType(serviceMethod)) {
      Optional<CodeBlock> requestWriter = getFactoryRequestWriterBlock(serviceMethod);
      requestWriter.ifPresent(constructorBuilder::addCode);
    }

    if (!isVoidType(serviceMethod.method.getReturnType())) {
      Optional<CodeBlock> responseReader = getResponseReader(serviceMethod);
      responseReader.ifPresent(constructorBuilder::addCode);
    }

    return constructorBuilder.build();
  }

  private Optional<CodeBlock> getRequestWriter(
      ExecutableElement method, TypeMirror requestBeanType, ServiceMethod serviceMethod) {
    CodeBlock.Builder builder = CodeBlock.builder();

    Writer annotation = method.getAnnotation(Writer.class);
    if (nonNull(annotation)) {
      Optional<TypeMirror> value =
          processorUtil.getClassValueFromAnnotation(method, Writer.class, "value");
      value.ifPresent(writerType -> builder.add("new $T()", TypeName.get(writerType)));
      return Optional.of(builder.build());
    } else if (consumesJson(serviceMethod) || consumesMultipartForm(serviceMethod)) {
      boolean serializerGenerated = !shouldGenerateSerializer(requestBeanType);

      CodeBlock instance =
          new FieldSerializerChainBuilder(requestBeanType, serializerGenerated)
              .getInstance(requestBeanType);
      TypeSpec.Builder writerType =
          TypeSpec.anonymousClassBuilder("$S", requestBeanType)
              .addSuperinterface(
                  ParameterizedTypeName.get(
                      ClassName.get(AbstractObjectWriter.class), TypeName.get(requestBeanType)))
              .addMethod(
                  MethodSpec.methodBuilder("newSerializer")
                      .addAnnotation(Override.class)
                      .addModifiers(Modifier.PROTECTED)
                      .returns(
                          ParameterizedTypeName.get(
                              ClassName.get(JsonSerializer.class), TypeVariableName.get("?")))
                      .addCode("return ")
                      .addCode(instance)
                      .addCode(";")
                      .build());

      builder.add("$L", writerType.build());
      return Optional.of(builder.build());
    } else if (consumesText(serviceMethod)) {
      builder.add("new $T()", StringWriter.class);
      return Optional.of(builder.build());
    }

    return Optional.empty();
  }

  private Optional<CodeBlock> getFactoryRequestWriterBlock(ServiceMethod serviceMethod) {
    Optional<CodeBlock> requestWriter =
        getRequestWriter(
            serviceMethod.method, getRequestBeanType(serviceMethod).type, serviceMethod);
    if (!requestWriter.isPresent()) {
      return Optional.empty();
    }
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.add("setRequestWriter(bean -> ").add(requestWriter.get()).add(".write(bean));");
    return Optional.of(builder.build());
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

  private boolean consumesMultipartForm(ServiceMethod serviceMethod) {
    String contentType = getContentType(serviceMethod);
    return contentType.contains(MediaType.MULTIPART_FORM_DATA);
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
      Optional<TypeMirror> value =
          processorUtil.getClassValueFromAnnotation(serviceMethod.method, Reader.class, "value");
      value.ifPresent(
          readerType ->
              builder.addStatement(
                  "setResponseReader(response -> new $T().read(response))",
                  TypeName.get(readerType)));
      return Optional.of(builder.build());
    } else if (isGenericResponse(serviceMethod)) {

      builder.addStatement(
          "setResponseReader(response -> new $T().read(response))",
          TypeName.get(GeneralResponseReader.class));
      return Optional.of(builder.build());
    } else if (producesJson(serviceMethod)) {

      TypeMirror responseBeanType = getResponseBeanType(serviceMethod);
      boolean deserializerGenerated = !shouldGenerateDeserializer(responseBeanType);
      CodeBlock instance =
          new FieldDeserializersChainBuilder(responseBeanType, deserializerGenerated)
              .getInstance(responseBeanType);
      TypeSpec.Builder readerType =
          TypeSpec.anonymousClassBuilder("$S", responseBeanType)
              .addSuperinterface(
                  ParameterizedTypeName.get(
                      ClassName.get(AbstractObjectReader.class), TypeName.get(responseBeanType)))
              .addMethod(
                  MethodSpec.methodBuilder("newDeserializer")
                      .addAnnotation(Override.class)
                      .addModifiers(Modifier.PROTECTED)
                      .returns(
                          ParameterizedTypeName.get(
                              ClassName.get(JsonDeserializer.class),
                              TypeName.get(responseBeanType)))
                      .addCode("return ")
                      .addCode(instance)
                      .addCode(";")
                      .build());

      builder.addStatement(
          "setResponseReader(response -> $L.read(response.getBodyAsString()))", readerType.build());
      return Optional.of(builder.build());
    } else if (producesText(serviceMethod)) {
      builder.addStatement(
          "setResponseReader(response -> new $T().read(response))",
          TypeName.get(StringReader.class));
      return Optional.of(builder.build());
    }

    return Optional.empty();
  }

  private boolean isGenericResponse(ServiceMethod serviceMethod) {
    return processorUtil.isAssignableFrom(getResponseBeanType(serviceMethod), Response.class);
  }

  private boolean producesJson(ServiceMethod serviceMethod) {
    String acceptResponse = getAcceptResponse(serviceMethod);
    return acceptResponse.contains(MediaType.APPLICATION_JSON)
        || acceptResponse.contains(MediaType.APPLICATION_JSON_PATCH_JSON);
  }

  private boolean producesText(ServiceMethod serviceMethod) {
    String acceptResponse = getAcceptResponse(serviceMethod);
    return acceptResponse.contains(MediaType.TEXT_PLAIN)
        || acceptResponse.contains(MediaType.TEXT_HTML)
        || acceptResponse.contains(MediaType.TEXT_XML);
  }

  private boolean isVoidType(ServiceMethod serviceMethod) {
    return getRequestBeanType(serviceMethod)._void;
  }

  private boolean isVoidType(TypeMirror type) {
    return TypeKind.VOID.equals(type.getKind())
        || types.isSameType(elements.getTypeElement(Void.class.getCanonicalName()).asType(), type);
  }

  private String getContentType(ServiceMethod serviceMethod) {
    if (nonNull(serviceMethod.method.getAnnotation(Consumes.class))) {
      return asContentTypeHeaderValue(serviceMethod.method.getAnnotation(Consumes.class).value());
    } else if (nonNull(findAnnotationInEnclosingElements(serviceElement, Consumes.class))) {
      return asAcceptsHeaderValue(
          findAnnotationInEnclosingElements(serviceElement, Consumes.class).value());
    }

    return "\"" + MediaType.APPLICATION_JSON + "\"";
  }

  private String asContentTypeHeaderValue(String[] contentTypes) {
    return Arrays.stream(contentTypes).map(s -> "\"" + s + "\"").collect(joining(","));
  }

  private String getAcceptResponse(ServiceMethod serviceMethod) {
    if (nonNull(serviceMethod.method.getAnnotation(Produces.class))) {
      return asAcceptsHeaderValue(serviceMethod.method.getAnnotation(Produces.class).value());
    } else if (nonNull(findAnnotationInEnclosingElements(serviceElement, Produces.class))) {
      return asAcceptsHeaderValue(
          findAnnotationInEnclosingElements(serviceElement, Produces.class).value());
    }
    return "\"" + MediaType.APPLICATION_JSON + "\"";
  }

  private String asAcceptsHeaderValue(String[] accepts) {
    return Arrays.stream(accepts).map(s -> "\"" + s + "\"").collect(joining(","));
  }

  public <A extends Annotation> A findAnnotationInEnclosingElements(
      Element element, Class<A> annotation) {
    A result = element.getAnnotation(annotation);
    if (Objects.nonNull(result) || element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
      return result;
    }
    return findAnnotationInEnclosingElements(element.getEnclosingElement(), annotation);
  }

  private String getPath(ServiceMethod serviceMethod) {
    Path pathAnnotation = serviceMethod.method.getAnnotation(Path.class);
    String methodPath = isNull(pathAnnotation) ? "" : pathAnnotation.value();
    return serviceMethod.servicePath
        + pathsSplitter(serviceMethod.servicePath, methodPath)
        + methodPath;
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

  private Optional<NullQueryParamStrategy> getNullQueryParamStrategy(
      TypeMirror requestBean, ServiceMethod serviceMethod) {
    if (nonNull(serviceMethod.method.getAnnotation(NullQueryStrategy.class))) {
      return Optional.of(serviceMethod.method.getAnnotation(NullQueryStrategy.class).value());
    }
    if (nonNull(requestBean.getAnnotation(NullQueryStrategy.class))) {
      return Optional.of(requestBean.getAnnotation(NullQueryStrategy.class).value());
    }
    return Optional.empty();
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
   * @param type the type
   * @return true if the type is one of the wrapper classes, false otherwise
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
      if (!_void && nonNull(param)) {
        return Optional.of(param.getSimpleName().toString());
      }
      return Optional.empty();
    }
  }
}

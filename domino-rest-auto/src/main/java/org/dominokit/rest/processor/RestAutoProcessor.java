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

import static java.util.Objects.nonNull;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.rest.shared.request.service.annotations.RestAuto;

/**
 * Annotation processor that reads OpenAPI definitions and emits {@link RequestFactory} interfaces.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class RestAutoProcessor extends AbstractProcessor {

  private Messager messager;
  private Filer filer;
  private Map<String, ClassName> generatedTypes = new ConcurrentHashMap<>();
  private Map<String, Schema<?>> componentSchemas = new LinkedHashMap<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(RestAuto.class.getCanonicalName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(RestAuto.class)) {
      if (element.getKind() != ElementKind.PACKAGE) {
        messager.printMessage(
            Diagnostic.Kind.ERROR, "@RestAuto can only be placed on package-info.java", element);
        continue;
      }
      PackageElement pkg = (PackageElement) element;
      RestAuto meta = element.getAnnotation(RestAuto.class);
      String resourcePath = meta.value();

      Optional<String> openApiContent = readResource(resourcePath);
      if (openApiContent.isEmpty()) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            "Could not read OpenAPI resource at path: " + resourcePath,
            element);
        continue;
      }

      OpenAPI openAPI = parse(openApiContent.get());
      if (openAPI == null) {
        messager.printMessage(
            Diagnostic.Kind.ERROR, "Failed to parse OpenAPI document: " + resourcePath, element);
        continue;
      }

      Map<String, List<GeneratedOperation>> operationsByTag = collectOperations(openAPI);
      cacheComponentSchemas(openAPI);
      generateComponents(pkg);
      for (Map.Entry<String, List<GeneratedOperation>> entry : operationsByTag.entrySet()) {
        try {
          writeInterface(pkg, entry.getKey(), entry.getValue());
        } catch (IOException ioException) {
          messager.printMessage(
              Diagnostic.Kind.ERROR,
              "Failed to write interface for tag "
                  + entry.getKey()
                  + " : "
                  + ioException.getMessage(),
              element);
        }
      }
    }
    return false;
  }

  private Optional<String> readResource(String path) {
    String normalized = path.startsWith("/") ? path.substring(1) : path;
    List<InputStream> attempts = new ArrayList<>();
    try {
      InputStream fromLoader = getClass().getClassLoader().getResourceAsStream(normalized);
      if (fromLoader != null) attempts.add(fromLoader);
    } catch (Exception ignored) {
    }

    try {
      FileObject fo = filer.getResource(StandardLocation.CLASS_PATH, "", normalized);
      attempts.add(fo.openInputStream());
    } catch (Exception ignored) {
    }

    try {
      FileObject fo = filer.getResource(StandardLocation.SOURCE_PATH, "", normalized);
      attempts.add(fo.openInputStream());
    } catch (Exception ignored) {
    }

    for (InputStream in : attempts) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
        return Optional.of(reader.lines().collect(Collectors.joining("\n")));
      } catch (Exception e) {
        // move to next attempt
      }
    }
    return Optional.empty();
  }

  private OpenAPI parse(String content) {
    try {
      return new OpenAPIV3Parser().readContents(content, null, null).getOpenAPI();
    } catch (Exception e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Error parsing OpenAPI: " + e.getMessage());
      return null;
    }
  }

  private Map<String, List<GeneratedOperation>> collectOperations(OpenAPI openAPI) {
    Map<String, List<GeneratedOperation>> grouped = new LinkedHashMap<>();
    if (openAPI.getPaths() == null) return grouped;

    openAPI
        .getPaths()
        .forEach(
            (path, item) -> {
              if (item == null) return;
              addOperation(grouped, path, PathItem.HttpMethod.GET, item.getGet());
              addOperation(grouped, path, PathItem.HttpMethod.POST, item.getPost());
              addOperation(grouped, path, PathItem.HttpMethod.PUT, item.getPut());
              addOperation(grouped, path, PathItem.HttpMethod.DELETE, item.getDelete());
              addOperation(grouped, path, PathItem.HttpMethod.PATCH, item.getPatch());
              addOperation(grouped, path, PathItem.HttpMethod.HEAD, item.getHead());
              addOperation(grouped, path, PathItem.HttpMethod.OPTIONS, item.getOptions());
              addOperation(grouped, path, PathItem.HttpMethod.TRACE, item.getTrace());
            });

    return grouped;
  }

  private void addOperation(
      Map<String, List<GeneratedOperation>> grouped,
      String path,
      PathItem.HttpMethod httpMethod,
      Operation operation) {
    if (operation == null) return;

    List<String> tags = operation.getTags();
    if (tags == null || tags.isEmpty()) {
      tags = Collections.singletonList("Default");
    }

    for (String tag : tags) {
      grouped
          .computeIfAbsent(tag, t -> new ArrayList<>())
          .add(new GeneratedOperation(path, httpMethod, operation));
    }
  }

  private void writeInterface(
      PackageElement pkg, String tagName, List<GeneratedOperation> operations) throws IOException {
    String interfaceName =
        toTypeName(tagName.endsWith("Resource") ? tagName : tagName + "Resource");

    TypeSpec.Builder iface =
        TypeSpec.interfaceBuilder(interfaceName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(RequestFactory.class)
            .addJavadoc("Generated from OpenAPI tag {@code $L}. Do not edit manually.\n", tagName);

    operations.stream()
        .sorted(Comparator.comparing(op -> op.operationId()))
        .forEach(op -> iface.addMethod(buildMethod(pkg, op)));

    JavaFile.builder(pkg.getQualifiedName().toString(), iface.build())
        .skipJavaLangImports(true)
        .build()
        .writeTo(filer);
  }

  private MethodSpec buildMethod(PackageElement pkg, GeneratedOperation op) {
    MethodSpec.Builder method =
        MethodSpec.methodBuilder(op.operationId())
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(determineResponseType(pkg, op.operation));

    method.addAnnotation(getHttpMethodAnnotation(op.httpMethod));
    method.addAnnotation(
        AnnotationSpec.builder(Path.class).addMember("value", "$S", op.path).build());

    List<String> consumes = new ArrayList<>();
    List<String> produces = new ArrayList<>();

    for (Parameter parameter : safeList(op.operation.getParameters())) {
      method.addParameter(buildParameter(pkg, parameter));
    }

    RequestBody requestBody = op.operation.getRequestBody();
    if (requestBody != null && requestBody.getContent() != null) {
      Map.Entry<String, MediaType> entry = requestBody.getContent().entrySet().iterator().next();
      consumes.add(entry.getKey());
      TypeName bodyType = mapSchema(modelPackage(pkg), entry.getValue().getSchema(), "body");
      method.addParameter(
          ParameterSpec.builder(bodyType, "body")
              .addAnnotation(
                  org.dominokit.rest.shared.request.service.annotations.RequestBody.class)
              .build());
    }

    ApiResponses responses = op.operation.getResponses();
    ApiResponse primary = pickPrimaryResponse(responses);
    if (primary != null && primary.getContent() != null) {
      produces.addAll(primary.getContent().keySet());
    }

    if (!consumes.isEmpty()) {
      method.addAnnotation(consumesAnnotation(consumes));
    }
    if (!produces.isEmpty()) {
      method.addAnnotation(producesAnnotation(produces));
    }

    return method.build();
  }

  private AnnotationSpec getHttpMethodAnnotation(PathItem.HttpMethod method) {
    ClassName annotation;
    switch (method) {
      case POST:
        annotation = ClassName.get(POST.class);
        break;
      case PUT:
        annotation = ClassName.get(PUT.class);
        break;
      case DELETE:
        annotation = ClassName.get(DELETE.class);
        break;
      case HEAD:
        annotation = ClassName.get(HEAD.class);
        break;
      case OPTIONS:
        annotation = ClassName.get(OPTIONS.class);
        break;
      case PATCH:
        annotation = ClassName.get(PATCH.class);
        break;
      case GET:
      default:
        annotation = ClassName.get(GET.class);
    }
    return AnnotationSpec.builder(annotation).build();
  }

  private ParameterSpec buildParameter(PackageElement pkg, Parameter parameter) {
    Schema<?> schema = parameter.getSchema();
    if (schema == null && parameter.getContent() != null && !parameter.getContent().isEmpty()) {
      schema = parameter.getContent().values().iterator().next().getSchema();
    }

    TypeName type = mapSchema(modelPackage(pkg), schema, parameter.getName());
    String name = sanitizeParameterName(parameter.getName());

    AnnotationSpec.Builder annotation;
    switch (parameter.getIn()) {
      case "path":
        annotation = AnnotationSpec.builder(PathParam.class);
        break;
      case "query":
        annotation = AnnotationSpec.builder(QueryParam.class);
        break;
      case "header":
        annotation = AnnotationSpec.builder(HeaderParam.class);
        break;
      case "cookie":
        annotation = AnnotationSpec.builder(CookieParam.class);
        break;
      default:
        annotation = AnnotationSpec.builder(QueryParam.class);
        break;
    }

    annotation.addMember("value", "$S", parameter.getName());

    return ParameterSpec.builder(type, name).addAnnotation(annotation.build()).build();
  }

  private ApiResponse pickPrimaryResponse(ApiResponses responses) {
    if (responses == null || responses.isEmpty()) return null;
    List<String> preferenceOrder = List.of("200", "201", "202", "default");
    for (String code : preferenceOrder) {
      ApiResponse resp = responses.get(code);
      if (resp != null) return resp;
    }
    return responses.values().iterator().next();
  }

  private TypeName determineResponseType(PackageElement pkg, Operation operation) {
    ApiResponse response = pickPrimaryResponse(operation.getResponses());
    if (response == null) {
      return ClassName.get(Void.class);
    }
    Content content = response.getContent();
    if (content == null || content.isEmpty()) {
      return ClassName.get(Void.class);
    }
    Schema<?> schema = content.values().iterator().next().getSchema();
    return mapSchema(modelPackage(pkg), schema, "response");
  }

  private AnnotationSpec consumesAnnotation(List<String> types) {
    AnnotationSpec.Builder builder = AnnotationSpec.builder(Consumes.class);
    builder.addMember(
        "value",
        "{$L}",
        types.stream().map(this::mediaTypeLiteral).collect(Collectors.joining(",")));
    return builder.build();
  }

  private AnnotationSpec producesAnnotation(List<String> types) {
    AnnotationSpec.Builder builder = AnnotationSpec.builder(Produces.class);
    builder.addMember(
        "value",
        "{$L}",
        types.stream().map(this::mediaTypeLiteral).collect(Collectors.joining(",")));
    return builder.build();
  }

  private String mediaTypeLiteral(String mediaType) {
    switch (mediaType.toLowerCase(Locale.ROOT)) {
      case "application/json":
        return "jakarta.ws.rs.core.MediaType.APPLICATION_JSON";
      case "text/plain":
        return "jakarta.ws.rs.core.MediaType.TEXT_PLAIN";
      case "text/html":
        return "jakarta.ws.rs.core.MediaType.TEXT_HTML";
      case "application/xml":
        return "jakarta.ws.rs.core.MediaType.APPLICATION_XML";
      case "application/octet-stream":
        return "jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM";
      default:
        return String.format("\"%s\"", mediaType);
    }
  }

  private void cacheComponentSchemas(OpenAPI openAPI) {
    if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
      openAPI
          .getComponents()
          .getSchemas()
          .forEach(
              (name, schema) -> {
                if (schema != null) {
                  componentSchemas.put(name, schema);
                }
              });
    }
  }

  private void generateComponents(PackageElement pkg) {
    String modelPkg = modelPackage(pkg);
    componentSchemas.forEach(
        (name, schema) -> {
          String key = qualifiedKey(modelPkg, name);
          if (generatedTypes.containsKey(key)) {
            return;
          }
          try {
            if (isEnumSchema(schema)) {
              TypeSpec enumType = buildEnum(modelPkg, name, schema);
              JavaFile.builder(modelPkg, enumType).skipJavaLangImports(true).build().writeTo(filer);
              generatedTypes.put(key, ClassName.get(modelPkg, enumType.name));
            } else {
              TypeSpec type = buildPojo(modelPkg, name, schema);
              JavaFile.builder(modelPkg, type).skipJavaLangImports(true).build().writeTo(filer);
              generatedTypes.put(key, ClassName.get(modelPkg, type.name));
            }
          } catch (IOException e) {
            messager.printMessage(
                Diagnostic.Kind.ERROR, "Failed generating type " + name + ": " + e.getMessage());
          }
        });
  }

  private TypeSpec buildPojo(String targetPackage, String name, Schema<?> schema) {
    TypeSpec.Builder type = TypeSpec.classBuilder(toTypeName(name)).addModifiers(Modifier.PUBLIC);
    if (schema.getProperties() != null) {
      schema
          .getProperties()
          .forEach(
              (prop, propSchema) -> {
                String fieldName = sanitizeParameterName(prop);
                TypeName fieldType = mapSchema(targetPackage, (Schema<?>) propSchema, fieldName);
                type.addField(fieldType, fieldName, Modifier.PRIVATE);
                String methodSuffix = toTypeName(fieldName);
                type.addMethod(
                    MethodSpec.methodBuilder("get" + methodSuffix)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(fieldType)
                        .addStatement("return this.$N", fieldName)
                        .build());
                type.addMethod(
                    MethodSpec.methodBuilder("set" + methodSuffix)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(fieldType, fieldName)
                        .addStatement("this.$N = $N", fieldName, fieldName)
                        .build());
              });
    }
    return type.build();
  }

  private TypeSpec buildEnum(String targetPackage, String name, Schema<?> schema) {
    TypeSpec.Builder enumBuilder =
        TypeSpec.enumBuilder(toTypeName(name)).addModifiers(Modifier.PUBLIC);
    schema
        .getEnum()
        .forEach(value -> enumBuilder.addEnumConstant(toEnumConstant(value.toString())));
    return enumBuilder.build();
  }

  private boolean isEnumSchema(Schema<?> schema) {
    return schema != null && schema.getEnum() != null && !schema.getEnum().isEmpty();
  }

  private TypeName mapSchema(String targetPackage, Schema<?> schema, String suggestedName) {
    if (schema == null) {
      return ClassName.get(String.class);
    }
    Schema<?> itemsCandidate =
        schema instanceof ArraySchema ? ((ArraySchema) schema).getItems() : schema.getItems();
    if (itemsCandidate != null
        || schema instanceof ArraySchema
        || "array".equals(schema.getType())) {
      Schema<?> items =
          itemsCandidate != null
              ? itemsCandidate
              : (schema instanceof ArraySchema ? ((ArraySchema) schema).getItems() : null);
      return ParameterizedTypeName.get(
          ClassName.get(List.class), mapSchema(targetPackage, items, suggestedName + "Item"));
    }

    String type = schema.getType();
    String format = schema.getFormat();
    if (schema instanceof BooleanSchema) {
      return ClassName.get(Boolean.class);
    }
    if (schema instanceof IntegerSchema) {
      if ("int64".equals(format)) {
        return ClassName.get(Long.class);
      }
      return ClassName.get(Integer.class);
    }
    if (schema instanceof NumberSchema) {
      if ("float".equals(format)) {
        return ClassName.get(Float.class);
      }
      if ("double".equals(format)) {
        return ClassName.get(Double.class);
      }
      return ClassName.get(java.math.BigDecimal.class);
    }
    if (schema instanceof StringSchema && !isEnumSchema(schema)) {
      if ("byte".equals(format) || "binary".equals(format)) {
        return ArrayTypeName.of(TypeName.BYTE);
      }
      return ClassName.get(String.class);
    }
    if (isEnumSchema(schema)) {
      String baseName =
          schema.getTitle() != null
              ? schema.getTitle()
              : (suggestedName != null ? suggestedName : "Enum");
      String enumName = toTypeName(baseName);
      String key = qualifiedKey(targetPackage, enumName);
      if (!generatedTypes.containsKey(key)) {
        try {
          TypeSpec enumType = buildEnum(targetPackage, enumName, schema);
          JavaFile.builder(targetPackage, enumType)
              .skipJavaLangImports(true)
              .build()
              .writeTo(filer);
          generatedTypes.put(key, ClassName.get(targetPackage, enumType.name));
        } catch (IOException e) {
          messager.printMessage(
              Diagnostic.Kind.ERROR,
              "Failed generating enum type " + enumName + ": " + e.getMessage());
        }
      }
      return generatedTypes.get(key);
    }

    if (schema.get$ref() != null) {
      String ref = schema.get$ref();
      String refName = ref.substring(ref.lastIndexOf('/') + 1);
      String key = qualifiedKey(targetPackage, refName);
      if (!generatedTypes.containsKey(key) && componentSchemas.containsKey(refName)) {
        Schema<?> refSchema = componentSchemas.get(refName);
        try {
          if (isEnumSchema(refSchema)) {
            TypeSpec enumType = buildEnum(targetPackage, refName, refSchema);
            JavaFile.builder(targetPackage, enumType)
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
            generatedTypes.put(key, ClassName.get(targetPackage, enumType.name));
          } else {
            TypeSpec typeSpec = buildPojo(targetPackage, refName, refSchema);
            JavaFile.builder(targetPackage, typeSpec)
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
            generatedTypes.put(key, ClassName.get(targetPackage, typeSpec.name));
          }
        } catch (IOException e) {
          messager.printMessage(
              Diagnostic.Kind.ERROR,
              "Failed generating ref type " + refName + ": " + e.getMessage());
        }
      }
      return generatedTypes.getOrDefault(key, ClassName.get(targetPackage, toTypeName(refName)));
    }

    if ("string".equals(type)) {
      if ("byte".equals(format) || "binary".equals(format)) {
        return ArrayTypeName.of(TypeName.BYTE);
      }
      return ClassName.get(String.class);
    }
    if ("integer".equals(type)) {
      if ("int64".equals(format)) {
        return ClassName.get(Long.class);
      }
      return ClassName.get(Integer.class);
    }
    if ("number".equals(type)) {
      if ("float".equals(format)) {
        return ClassName.get(Float.class);
      }
      if ("double".equals(format)) {
        return ClassName.get(Double.class);
      }
      return ClassName.get(java.math.BigDecimal.class);
    }
    if ("boolean".equals(type)) {
      return ClassName.get(Boolean.class);
    }
    if ("object".equals(type)
        || (schema.getProperties() != null && !schema.getProperties().isEmpty())) {
      if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
        String inlineName =
            suggestedName != null ? toTypeName(suggestedName + "Object") : "InlineObject";
        String key = qualifiedKey(targetPackage, inlineName);
        if (!generatedTypes.containsKey(key)) {
          TypeSpec inline = buildPojo(targetPackage, inlineName, schema);
          try {
            JavaFile.builder(targetPackage, inline)
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
            generatedTypes.put(key, ClassName.get(targetPackage, inline.name));
          } catch (IOException e) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Failed generating inline object " + inlineName + ": " + e.getMessage());
          }
        }
        return generatedTypes.get(key);
      }
      return ParameterizedTypeName.get(
          ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(Object.class));
    }

    return ClassName.get(String.class);
  }

  private String sanitizeParameterName(String name) {
    if (name == null || name.isEmpty()) {
      return "param";
    }
    String cleaned = name.replaceAll("[^A-Za-z0-9_]", "_");
    if (cleaned.isEmpty()) cleaned = "param";
    if (Character.isDigit(cleaned.charAt(0))) cleaned = "p" + cleaned;
    return cleaned;
  }

  private String toTypeName(String raw) {
    StringBuilder out = new StringBuilder();
    boolean capitalize = true;
    for (char c : raw.toCharArray()) {
      if (!Character.isJavaIdentifierPart(c)) {
        capitalize = true;
        continue;
      }
      if (out.length() == 0 && !Character.isJavaIdentifierStart(c)) {
        out.append('T');
      }
      out.append(capitalize ? Character.toUpperCase(c) : c);
      capitalize = false;
    }
    if (out.length() == 0) {
      return "GeneratedResource";
    }
    return out.toString();
  }

  private static String sanitizeMethodName(String raw) {
    String base = raw == null || raw.isBlank() ? "operation" : raw;
    StringBuilder result = new StringBuilder();
    boolean nextUpper = false;
    for (char c : base.toCharArray()) {
      if (Character.isJavaIdentifierPart(c)) {
        if (result.length() == 0 && !Character.isJavaIdentifierStart(c)) {
          result.append('m');
        }
        result.append(nextUpper ? Character.toUpperCase(c) : c);
        nextUpper = false;
      } else {
        nextUpper = true;
      }
    }
    if (result.length() == 0) return "operation";
    result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
    return result.toString();
  }

  private String qualifiedKey(String pkgName, String simpleName) {
    return pkgName + ":" + simpleName;
  }

  private String toEnumConstant(String value) {
    String candidate = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
    if (candidate.isBlank()) {
      candidate = "VALUE";
    }
    if (Character.isDigit(candidate.charAt(0))) {
      candidate = "_" + candidate;
    }
    return candidate;
  }

  private String modelPackage(PackageElement pkg) {
    return pkg.getQualifiedName().toString() + ".model";
  }

  private List<Parameter> safeList(List<Parameter> parameters) {
    return parameters == null ? Collections.emptyList() : parameters;
  }

  private static final class GeneratedOperation {
    private final String path;
    private final PathItem.HttpMethod httpMethod;
    private final Operation operation;
    private final String operationId;

    private GeneratedOperation(String path, PathItem.HttpMethod httpMethod, Operation operation) {
      this.path = path;
      this.httpMethod = httpMethod;
      this.operation = operation;
      this.operationId =
          sanitizeMethodName(
              nonNull(operation.getOperationId())
                  ? operation.getOperationId()
                  : httpMethod.name().toLowerCase(Locale.ROOT) + path);
    }

    public String operationId() {
      return operationId;
    }
  }
}

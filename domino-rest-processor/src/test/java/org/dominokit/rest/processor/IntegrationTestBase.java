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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.dominokit.rest.DominoRestConfig;
import org.dominokit.rest.shared.request.DominoRestContext;
import org.dominokit.rest.shared.request.ServerRequest;
import org.dominokit.rest.shared.request.Success;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for integration tests that require an embedded JAX-RS server. Starts a Jersey server
 * on a random port with all test resources registered.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

  protected HttpServer server;
  protected int port;
  protected String baseUrl;

  @BeforeAll
  void setupServer() {
    // Initialize Domino REST context
    DominoRestContext.make().init(DominoRestConfig.initDefaults());

    // Create Jersey resource config with all test resources
    ResourceConfig config = new ResourceConfig();
    registerResources(config);
    config.register(JacksonFeature.class);
    config
        .getConfiguration()
        .getResources()
        .forEach(
            resource -> {
              System.out.println(resource.getPath());
            });
    // Start server on random port
    URI baseUri = URI.create("http://localhost:0/");
    server = JdkHttpServerFactory.createHttpServer(baseUri, config, false);
    server.start();

    // Get the actual port assigned
    port = server.getAddress().getPort();
    baseUrl = "http://localhost:" + port;

    System.out.println("=".repeat(80));
    System.out.println("Test server started on: " + baseUrl);
    System.out.println("=".repeat(80));
    logRegisteredResources(config);
    System.out.println("=".repeat(80));
  }

  /** Override this method to register JAX-RS resources for your tests. */
  protected abstract void registerResources(ResourceConfig config);

  @AfterAll
  void teardownServer() {
    if (server != null) {
      server.stop(0);
      System.out.println("Test server stopped");
    }
  }

  /** Helper class to capture request results. */
  protected static class Result<S> {
    public S successBody;
    public Throwable error;

    public boolean isSuccess() {
      return error == null;
    }

    public boolean isError() {
      return error != null;
    }
  }

  /**
   * Execute a ServerRequest and wait for completion.
   *
   * @param request the request to execute
   * @param <R> request body type
   * @param <S> response body type
   * @return Result containing either success body or error
   * @throws InterruptedException if waiting is interrupted
   */
  protected <R, S> Result<S> executeRequest(ServerRequest<R, S> request)
      throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Result<S> result = new Result<>();

    request
        .onSuccess(
            (Success<S>)
                body -> {
                  result.successBody = body;
                })
        .onFailed(err -> result.error = err.getThrowable())
        .onComplete(latch::countDown);

    request.send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete within 10 seconds");

    return result;
  }

  /** Execute a ServerRequest with custom timeout. */
  protected <R, S> Result<S> executeRequest(ServerRequest<R, S> request, long timeoutSeconds)
      throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Result<S> result = new Result<>();

    request
        .onSuccess((Success<S>) body -> result.successBody = body)
        .onFailed(err -> result.error = err.getThrowable())
        .onComplete(latch::countDown);

    request.send();

    assertTrue(
        latch.await(timeoutSeconds, TimeUnit.SECONDS),
        "Request did not complete within " + timeoutSeconds + " seconds");

    return result;
  }

  /** Helper to assert successful request with body check. */
  protected <S> void assertSuccess(Result<S> result, String message) {
    if (result.error != null) {
      throw new AssertionError(message + " - Error: " + result.error.getMessage(), result.error);
    }
    assertTrue(result.isSuccess(), message);
  }

  /** Helper to assert error occurred. */
  protected <S> void assertError(Result<S> result, String message) {
    assertTrue(result.isError(), message);
  }

  /** Logs all registered JAX-RS resources and their endpoints. */
  private void logRegisteredResources(ResourceConfig config) {
    Set<Class<?>> resourceClasses = config.getClasses();

    if (resourceClasses.isEmpty()) {
      System.out.println("No resources registered!");
      return;
    }

    System.out.println("Registered Resources:");
    System.out.println();

    List<EndpointInfo> endpoints = new ArrayList<>();

    for (Class<?> resourceClass : resourceClasses) {
      if (!isJaxRsResource(resourceClass)) {
        continue;
      }

      String classPath = "";
      if (resourceClass.isAnnotationPresent(Path.class)) {
        classPath = resourceClass.getAnnotation(Path.class).value();
      }

      System.out.println("  Resource: " + resourceClass.getSimpleName());
      if (!classPath.isEmpty()) {
        System.out.println("    Base Path: " + classPath);
      }

      // Scan all methods for endpoint annotations
      for (Method method : resourceClass.getDeclaredMethods()) {
        String httpMethod = getHttpMethod(method);
        if (httpMethod != null) {
          String methodPath = "";
          if (method.isAnnotationPresent(Path.class)) {
            methodPath = method.getAnnotation(Path.class).value();
          }

          String fullPath = combinePaths(classPath, methodPath);
          String consumes = getConsumes(method);
          String produces = getProduces(method);

          endpoints.add(
              new EndpointInfo(httpMethod, fullPath, consumes, produces, method.getName()));
        }
      }
      System.out.println();
    }

    // Print endpoint summary table
    if (!endpoints.isEmpty()) {
      System.out.println("Endpoint Summary:");
      System.out.println();
      System.out.printf("  %-8s %-60s %-20s%n", "METHOD", "PATH", "HANDLER");
      System.out.println("  " + "-".repeat(90));

      endpoints.sort(Comparator.comparing(e -> e.path));

      for (EndpointInfo endpoint : endpoints) {
        System.out.printf(
            "  %-8s %-60s %-20s%n", endpoint.method, endpoint.path, endpoint.handlerMethod);
      }
      System.out.println();
    }
  }

  private boolean isJaxRsResource(Class<?> clazz) {
    return clazz.isAnnotationPresent(Path.class);
  }

  private String getHttpMethod(Method method) {
    if (method.isAnnotationPresent(GET.class)) return "GET";
    if (method.isAnnotationPresent(POST.class)) return "POST";
    if (method.isAnnotationPresent(PUT.class)) return "PUT";
    if (method.isAnnotationPresent(DELETE.class)) return "DELETE";
    if (method.isAnnotationPresent(PATCH.class)) return "PATCH";
    if (method.isAnnotationPresent(HEAD.class)) return "HEAD";
    if (method.isAnnotationPresent(OPTIONS.class)) return "OPTIONS";
    return null;
  }

  private String getConsumes(Method method) {
    if (method.isAnnotationPresent(Consumes.class)) {
      String[] values = method.getAnnotation(Consumes.class).value();
      return values.length > 0 ? String.join(", ", values) : "";
    }
    return "";
  }

  private String getProduces(Method method) {
    if (method.isAnnotationPresent(Produces.class)) {
      String[] values = method.getAnnotation(Produces.class).value();
      return values.length > 0 ? String.join(", ", values) : "";
    }
    return "";
  }

  private String combinePaths(String basePath, String methodPath) {
    if (basePath.isEmpty()) return methodPath;
    if (methodPath.isEmpty()) return basePath;

    // Normalize slashes
    String base = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
    String method = methodPath.startsWith("/") ? methodPath : "/" + methodPath;

    return base + method;
  }

  /** Simple data class to hold endpoint information. */
  private static class EndpointInfo {
    final String method;
    final String path;
    final String consumes;
    final String produces;
    final String handlerMethod;

    EndpointInfo(
        String method, String path, String consumes, String produces, String handlerMethod) {
      this.method = method;
      this.path = path;
      this.consumes = consumes;
      this.produces = produces;
      this.handlerMethod = handlerMethod;
    }
  }
}

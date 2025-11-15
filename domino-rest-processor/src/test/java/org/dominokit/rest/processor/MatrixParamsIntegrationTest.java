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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.dominokit.rest.processor.resources.MatrixParamsResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Matrix Parameters functionality using generated request factories. Tests
 * that the annotation processor correctly generates code for @MatrixParam annotations and that the
 * generated requests work correctly with a real JAX-RS server.
 */
public class MatrixParamsIntegrationTest extends IntegrationTestBase {

  @Override
  protected void registerResources(ResourceConfig config) {
    config.register(MatrixParamsResource.class);
  }

  @Test
  void testSingleMatrixParameter() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksByCategory("fiction")
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("category");
    assertThat(responseRef.get().toString()).contains("fiction");
  }

  @Test
  void testMultipleMatrixParameters() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksByAuthorAndYear("Smith", 2020)
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("Smith");
    assertThat(responseRef.get().toString()).contains("2020");
  }

  @Test
  void testMatrixParamsWithPathParams() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksInLibrary("lib123", "science")
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("lib123");
    assertThat(responseRef.get().toString()).contains("science");
  }

  @Test
  void testMatrixParamsWithQueryParams() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksByAuthorPaginated("Doe", 1, 20)
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("Doe");
    assertThat(responseRef.get().toString()).contains("1");
    assertThat(responseRef.get().toString()).contains("20");
  }

  @Test
  void testMultiValuedMatrixParameter() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksByGenres(Arrays.asList("fiction", "mystery", "thriller"))
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("fiction");
    assertThat(responseRef.get().toString()).contains("mystery");
    assertThat(responseRef.get().toString()).contains("thriller");
  }

  @Test
  void testNestedPathsWithMatrixParams() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getBooksInShelf("store1", "shelf-A", "new")
        .setServiceRoot(baseUrl)
        .onSuccess(
            books -> {
              responseRef.set(books);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("store1");
    assertThat(responseRef.get().toString()).contains("shelf-A");
    assertThat(responseRef.get().toString()).contains("new");
  }

  @Test
  void testPostWithMatrixParam() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    SampleRequest bookData = new SampleRequest();
    MatrixParamsServiceFactory.INSTANCE
        .createBook("hardcover", bookData)
        .setServiceRoot(baseUrl)
        .onSuccess(
            response -> {
              responseRef.set(response);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().getMatrixParameters().get("format").get(0)).isEqualTo("hardcover");
  }

  @Test
  void testMatrixParamsWithDifferentTypes() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .filterProducts(10.0, 100.0, true)
        .setServiceRoot(baseUrl)
        .onSuccess(
            products -> {
              responseRef.set(products);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("10.0");
    assertThat(responseRef.get().toString()).contains("100.0");
    assertThat(responseRef.get().toString()).contains("true");
  }

  @Test
  void testComplexScenarioWithAllParamTypes() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    MatrixParamsServiceFactory.INSTANCE
        .getInventory("warehouse1", "available", true, "token123")
        .setServiceRoot(baseUrl)
        .onSuccess(
            inventory -> {
              responseRef.set(inventory);
            })
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(() -> latch.countDown())
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertThat(responseRef.get().toString()).contains("warehouse1");
    assertThat(responseRef.get().toString()).contains("available");
    assertThat(responseRef.get().toString()).contains("true");
    assertThat(responseRef.get().toString()).contains("token123");
  }
}

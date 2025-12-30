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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.dominokit.rest.processor.resources.RegexPathParamsResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Regex Path Parameters functionality using generated request factories.
 * Tests that the annotation processor correctly generates code for regex-constrained path
 * parameters and that the generated requests work correctly with a real JAX-RS server.
 */
public class RegexPathParamsIntegrationTest extends IntegrationTestBase {

  @Override
  protected void registerResources(ResourceConfig config) {
    config.register(RegexPathParamsResource.class);
  }

  @Test
  void testDigitsOnlyRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    var request = RegexPathParamsServiceFactory.INSTANCE.getUserById("12345");
    request.setServiceRoot(baseUrl);

    request
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              System.err.println(
                  "DEBUG: Request failed with error: "
                      + failedResponseBean.getThrowable().getMessage());
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("12345", responseRef.get().getPathParameters().get("id"));
  }

  @Test
  void testUuidRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getOrderById("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals(
        "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        responseRef.get().getPathParameters().get("orderId"));
  }

  @Test
  void testAlphanumericRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getProductByCode("ABC123XYZ")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("ABC123XYZ", responseRef.get().getPathParameters().get("code"));
  }

  @Test
  void testMultipleRegexPathParams() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getStoreItem("ABC", "12345")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("ABC", responseRef.get().getPathParameters().get("storeCode"));
    assertEquals("12345", responseRef.get().getPathParameters().get("itemId"));
  }

  @Test
  void testEmailRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getContactByEmail("user@example.com")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("user@example.com", responseRef.get().getPathParameters().get("email"));
  }

  @Test
  void testDateFormatRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getReportByDate("2024-01-15")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("2024-01-15", responseRef.get().getPathParameters().get("date"));
  }

  @Test
  void testSemanticVersionRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getApiDocs("v1.2.3")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("v1.2.3", responseRef.get().getPathParameters().get("version"));
  }

  @Test
  void testSlugRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getBlogPost("my-first-post-2024")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("my-first-post-2024", responseRef.get().getPathParameters().get("slug"));
  }

  @Test
  void testIpAddressRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getDeviceByIp("192.168.1.100")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("192.168.1.100", responseRef.get().getPathParameters().get("ip"));
  }

  @Test
  void testColonSyntaxWithRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getArticleById("98765")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("98765", responseRef.get().getPathParameters().get("articleId"));
  }

  @Test
  void testComplexNestedRegex() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getEmployee("1001", "IT", "A123456")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("1001", responseRef.get().getPathParameters().get("orgId"));
    assertEquals("IT", responseRef.get().getPathParameters().get("deptCode"));
    assertEquals("A123456", responseRef.get().getPathParameters().get("empId"));
  }

  @Test
  void testRegexWithQueryParam() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .getTransaction("TX1234567890", "json")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertTrue(responseRef.get().getQueryParameters().get("format").contains("json"));
    assertEquals("TX1234567890", responseRef.get().getPathParameters().get("txnId"));
  }

  @Test
  void testPostWithRegexPath() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    SampleRequest transaction = new SampleRequest();
    RegexPathParamsServiceFactory.INSTANCE
        .createTransaction("123456789", transaction)
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("123456789", responseRef.get().getPathParameters().get("accountNum"));
  }

  @Test
  void testPutWithRegexPath() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    SampleRequest inventoryData = new SampleRequest();
    RegexPathParamsServiceFactory.INSTANCE
        .updateInventory("ABC-1234-XY", inventoryData)
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertNotNull(responseRef.get(), "Response should not be null");
    assertEquals("ABC-1234-XY", responseRef.get().getPathParameters().get("sku"));
  }

  @Test
  void testDeleteWithRegexPath() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<TestResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    RegexPathParamsServiceFactory.INSTANCE
        .clearCache("cache-key-123")
        .setServiceRoot(baseUrl)
        .onSuccess(responseRef::set)
        .onFailed(
            failedResponseBean -> {
              errorRef.set(failedResponseBean.getThrowable());
            })
        .onComplete(latch::countDown)
        .send();

    assertTrue(latch.await(10, TimeUnit.SECONDS), "Request did not complete in time");
    assertEquals("cache-key-123", responseRef.get().getPathParameters().get("key"));
  }
}

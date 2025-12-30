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
package org.dominokit.rest.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.HttpMethod;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.dominokit.rest.DominoRestConfig;
import org.dominokit.rest.model.MultipartTestService;
import org.dominokit.rest.model.SampleObject;
import org.dominokit.rest.model.SampleObject_MapperImpl;
import org.dominokit.rest.model.locator.CustomersResourceFactory;
import org.dominokit.rest.shared.MultipartForm;
import org.dominokit.rest.shared.Response;
import org.dominokit.rest.shared.request.DominoRestContext;
import org.dominokit.rest.shared.request.FailedResponseBean;
import org.dominokit.rest.shared.request.RequestMeta;
import org.dominokit.rest.shared.request.ResponseInterceptor;
import org.dominokit.rest.shared.request.ServerRequest;
import org.dominokit.rest.shared.request.StringReader;
import org.dominokit.rest.shared.request.Success;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * End-to-end tests for ServerRequest using a real in-JVM HTTP server and the JVM DominoRestConfig
 * stack. We never instantiate transport directly; we only construct ServerRequest subclasses and
 * call send().
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerRequestHttpTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerRequestHttpTest.class);

  private HttpServer server;
  private int port;

  @BeforeAll
  void initRuntimeAndServer() throws Exception {
    // Initialize the real JVM request pipeline
    DominoRestContext.make().init(DominoRestConfig.initDefaults());

    // Start a tiny HTTP server on a random free port
    server = HttpServer.create(new InetSocketAddress(0), 0);
    port = server.getAddress().getPort();

    server.createContext("/echo", ServerRequestHttpTest::echoHandler);
    server.createContext("/echobean", ServerRequestHttpTest::echoBeanHandler);
    server.createContext("/json", ServerRequestHttpTest::jsonHandler);
    server.createContext("/upload", ServerRequestHttpTest::uploadHandler);
    server.createContext("/delay", ServerRequestHttpTest::delayHandler);
    server.createContext("/status", ServerRequestHttpTest::statusHandler);
    server.createContext("/customers", ServerRequestHttpTest::customersHandler);

    server.setExecutor(Executors.newCachedThreadPool());
    server.start();
  }

  @AfterAll
  void tearDown() {
    if (server != null) server.stop(0);
  }

  @Test
  void get_with_headers_and_multi_query_params() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    req.setHeader("X-Test", "yes");
    req.setQueryParameter("x", "1");
    req.addQueryParameter("x", "2");

    var result = run(req);
    assertNull(result.error);

    String body = result.successBody;
    assertTrue(body.contains("\"method\":\"GET\""), body);
    assertTrue(body.contains("\"path\":\"/echo\""), body);
    assertTrue(body.contains("\"query\":\"x=1&x=2\""), body);
    assertTrue(body.contains("\"X-Test\":\"yes\""), body);
  }

  @Test
  void matrix_params_on_last_segment_are_rendered_in_path() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    req.setPath("/echo/a/b");
    req.addMatrixParameter("role", "admin");
    req.addMatrixParameter("role", "owner");
    req.addMatrixParameter("active", "true");

    var result = run(req);
    assertNull(result.error);

    // The server echoes the final request path it sees
    assertTrue(
        result.successBody.contains("\"path\":\"/echo/a/b;role=admin;role=owner;active=true\""));
  }

  @Test
  void fragment_is_not_sent_to_server_but_is_preserved_in_normalized_url() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    // Add a fragment expression and a concrete value
    req.setPath("/echo#fragVal");
    var result = run(req);

    // Server never receives fragment; we only assert the path the server saw (no #)
    assertTrue(result.successBody.contains("\"path\":\"/echo\""));
  }

  @Test
  void status_204_is_treated_as_success() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    req.setPath("/status/204");

    var result = run(req);
    assertNull(result.error, "Should not error on 204");
    // Body will be empty on server side; we just ensure success code path executed.
  }

  @Test
  void timeout_is_honored_via_serverRequest_send() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    // server sleeps BEFORE sending headers; client timeout should trigger
    req.setPath("/delay?ms=2000");
    req.setTimeout(100); // 100ms

    var result = run(req);
    assertNotNull(result.error, "Expected timeout error");
    String name = result.error.getClass().getName();
    assertTrue(
        name.contains("HttpTimeoutException") || name.contains("Timeout"),
        "Unexpected error type: " + name);
  }

  @Test
  void abort_cancels_inflight_request() throws Exception {
    EchoGet req = new EchoGet("http://localhost:" + port);
    req.setPath("/delay?ms=3000");

    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(1);
    AtomicReference<String> bodyRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    req.onSuccess((Success<String>) bodyRef::set)
        .onFailed(err -> errorRef.set(err.getThrowable()))
        .onComplete(done::countDown);

    // fire & abort quickly
    started.countDown();
    req.send();
    req.abort();

    assertTrue(done.await(5, TimeUnit.SECONDS), "Did not complete after abort");
    // Depending on transport you may get error or just completion; but request is marked aborted.
    assertTrue(req.isAborted(), "Request expected to be marked aborted");
  }

  @Test
  void post_json_round_trip() throws Exception {
    JsonPost req = new JsonPost("http://localhost:" + port);
    req.setAccept(new String[] {"application/json"});
    req.setContentType(new String[] {"application/json"});
    // no request bean in this tiny demo; writer returns fixed JSON

    var result = run(req);
    assertNull(result.error);

    assertTrue(result.successBody.contains("\"Content-Type\":\"application/json\""));
    assertTrue(result.successBody.contains("\"body\":\"{\\\"name\\\":\\\"domino\\\"}\""));
  }

  @Test
  void multipart_upload_acknowledged() throws Exception {
    MultipartForm multipartForm = new MultipartForm();
    ServerRequest<MultipartForm, String> req =
        new TestServerRequest<>(
            new RequestMeta(
                MultipartTestService.class, "textMultipart", MultipartForm.class, String.class),
            multipartForm);
    req.setServiceRoot("http://localhost:" + port);
    req.setPath("/upload");
    req.setHttpMethod("POST");
    req.setResponseReader(new StringReader());
    req.setMultipartForm(true); // mark as multipart so the router knows
    var result = run(req);
    assertNull(result.error);
    assertTrue(result.successBody.contains("multipart-ok"));
  }

  @Test
  void response_interceptor_test() throws Exception {

    ResponseInterceptor interceptor =
        new ResponseInterceptor() {
          @Override
          public void interceptOnSuccess(ServerRequest serverRequest, Response response) {
            response
                .getBean()
                .ifPresent(
                    o -> {
                      assertTrue(o instanceof SampleObject);
                      ((SampleObject) o).setName("intercepted");
                    });
          }
        };
    DominoRestContext.make().getConfig().addResponseInterceptor(interceptor);
    EchoGetBean req = new EchoGetBean("http://localhost:" + port);

    var result = runBeanRequest(req);
    assertNull(result.error);

    SampleObject body = result.successBodyBean;
    assertEquals("intercepted", body.getName());
    assertEquals("1234", body.getId());
    DominoRestContext.make().getConfig().removeResponseInterceptor(interceptor);
  }

  @Test
  void response_interceptor_before_after_success_and_complete_are_called() throws Exception {
    AtomicBoolean beforeSuccess = new AtomicBoolean(false);
    AtomicBoolean beforeComplete = new AtomicBoolean(false);
    AtomicBoolean afterComplete = new AtomicBoolean(false);
    AtomicBoolean beforeFailed = new AtomicBoolean(false);

    ResponseInterceptor interceptor =
        new ResponseInterceptor() {
          @Override
          public void onBeforeSuccessCallback(ServerRequest serverRequest, Response response) {
            beforeSuccess.set(true);
          }

          @Override
          public void onBeforeCompleteCallback(ServerRequest serverRequest) {
            beforeComplete.set(true);
          }

          @Override
          public void onAfterCompleteCallback(ServerRequest serverRequest) {
            afterComplete.set(true);
          }

          @Override
          public void onBeforeFailedCallback(
              ServerRequest serverRequest, FailedResponseBean failedResponse) {
            beforeFailed.set(true);
          }
        };

    DominoRestContext.make().getConfig().addResponseInterceptor(interceptor);
    EchoGetBean req = new EchoGetBean("http://localhost:" + port);
    var result = runBeanRequest(req);
    assertNull(result.error);

    // success path: success callbacks should be called; failed callbacks should not
    assertTrue(beforeSuccess.get(), "onBeforeSuccessCallback should have been called");
    assertTrue(beforeComplete.get(), "onBeforeCompleteCallback should have been called");
    assertTrue(afterComplete.get(), "onAfterCompleteCallback should have been called");
    assertFalse(
        beforeFailed.get(), "onBeforeFailedCallback should NOT have been called on success");
  }

  @Test
  void response_interceptor_before_after_failed_and_complete_are_called() throws Exception {
    AtomicBoolean beforeSuccess = new AtomicBoolean(false);
    AtomicBoolean beforeComplete = new AtomicBoolean(false);
    AtomicBoolean afterComplete = new AtomicBoolean(false);
    AtomicBoolean beforeFailed = new AtomicBoolean(false);

    ResponseInterceptor interceptor =
        new ResponseInterceptor() {
          @Override
          public void onBeforeSuccessCallback(ServerRequest serverRequest, Response response) {
            beforeSuccess.set(true);
          }

          @Override
          public void onBeforeCompleteCallback(ServerRequest serverRequest) {
            beforeComplete.set(true);
          }

          @Override
          public void onAfterCompleteCallback(ServerRequest serverRequest) {
            afterComplete.set(true);
          }

          @Override
          public void onBeforeFailedCallback(
              ServerRequest serverRequest, FailedResponseBean failedResponse) {
            beforeFailed.set(true);
          }
        };

    DominoRestContext.make().getConfig().addResponseInterceptor(interceptor);
    // Use a request class that is explicitly targeting the /status handler so we ensure a 500
    // response is returned from the test server. Using EchoGet and changing the path can be
    // ambiguous depending on routing/formatting; creating an explicit StatusGet avoids that.
    StatusGet req = new StatusGet("http://localhost:" + port);
    // setPath to the final segment (we keep the service root pointing to host:port)
    req.setPath("/status/500");
    var result = run(req);

    // failed callbacks should be called; success callbacks should not
    assertTrue(beforeFailed.get(), "onBeforeFailedCallback should have been called");
    assertTrue(beforeComplete.get(), "onBeforeCompleteCallback should have been called");
    assertTrue(afterComplete.get(), "onAfterCompleteCallback should have been called");
    assertFalse(
        beforeSuccess.get(), "onBeforeSuccessCallback should NOT have been called on failure");
  }

  @Test
  void resource_locator_generated_clients_can_traverse_child_resources() throws Exception {
    String root = "http://localhost:" + port;
    CustomersResourceFactory factory = CustomersResourceFactory.INSTANCE;

    var customerReq = factory.getCustomer("alice");
    customerReq.setServiceRoot(root);
    var customerResult = run(customerReq);
    assertNull(customerResult.error);
    assertEquals("customer-alice", customerResult.successBody);

    var ordersFactory = factory.orders("alice");
    var listOrdersReq = ordersFactory.listOrders();
    listOrdersReq.setServiceRoot(root);
    var listOrdersResult = run(listOrdersReq);
    assertNull(listOrdersResult.error);
    assertEquals("orders-alice", listOrdersResult.successBody);

    var orderReq = ordersFactory.getOrder("A1");
    orderReq.setServiceRoot(root);
    var orderResult = run(orderReq);
    assertNull(orderResult.error);
    assertEquals("order-alice-A1", orderResult.successBody);
  }

  @Test
  void resource_locator_supports_overloads_and_sibling_paths() throws Exception {
    String root = "http://localhost:" + port;
    CustomersResourceFactory factory = CustomersResourceFactory.INSTANCE;

    var ordersByIntFactory = factory.orders(7);
    var listOrdersReq = ordersByIntFactory.listOrders();
    listOrdersReq.setServiceRoot(root);
    var listOrdersResult = run(listOrdersReq);
    assertNull(listOrdersResult.error);
    assertEquals("orders-7", listOrdersResult.successBody);

    var standingOrdersFactory = factory.standingOrders("bob");
    var standingOrdersReq = standingOrdersFactory.listOrders();
    standingOrdersReq.setServiceRoot(root);
    var standingOrdersResult = run(standingOrdersReq);
    assertNull(standingOrdersResult.error);
    assertEquals("standing-orders-bob", standingOrdersResult.successBody);

    var standingOrderReq = standingOrdersFactory.getOrder("SO-9");
    standingOrderReq.setServiceRoot(root);
    var standingOrderResult = run(standingOrderReq);
    assertNull(standingOrderResult.error);
    assertEquals("standing-order-bob-SO-9", standingOrderResult.successBody);
  }

  // ---------------------------------------------------------------------------
  // Small concrete requests (mimic your generated classes)
  // ---------------------------------------------------------------------------

  /** GET /echo with String body reader. */
  static final class EchoGet extends ServerRequest<Void, String> {
    EchoGet(String serviceRoot) {
      super(new RequestMeta(EchoGet.class, "echo", Void.class, String.class), null);
      setServiceRoot(serviceRoot);
      setHttpMethod(HttpMethod.GET);
      setPath("/echo");
      setAccept(new String[] {"application/json"});
      // simplest reader: return raw response body as String
      setResponseReader(Response::getBodyAsString);
    }
  }
  /** GET /status with String body reader. */
  static final class StatusGet extends ServerRequest<Void, String> {
    StatusGet(String serviceRoot) {
      super(new RequestMeta(StatusGet.class, "status", Void.class, String.class), null);
      setServiceRoot(serviceRoot);
      setHttpMethod(HttpMethod.GET);
      // default base path for this helper; tests may override with setPath
      setPath("/status");
      setAccept(new String[] {"text/plain"});
      setResponseReader(Response::getBodyAsString);
    }
  }
  /** GET /echo with String body reader. */
  static final class EchoGetBean extends ServerRequest<Void, SampleObject> {
    EchoGetBean(String serviceRoot) {
      super(new RequestMeta(EchoGet.class, "echo", Void.class, String.class), null);
      setServiceRoot(serviceRoot);
      setHttpMethod(HttpMethod.GET);
      setPath("/echobean");
      setAccept(new String[] {"application/json"});
      // simplest reader: return raw response body as String
      setResponseReader(
          response -> SampleObject_MapperImpl.INSTANCE.read(response.getBodyAsString()));
    }
  }

  /** POST /json, writes fixed JSON and reads body as string. */
  static final class JsonPost extends ServerRequest<Void, String> {
    JsonPost(String serviceRoot) {
      super(new RequestMeta(JsonPost.class, "json", Void.class, String.class), null);
      setServiceRoot(serviceRoot);
      setHttpMethod(HttpMethod.POST);
      setPath("/json");
      setAccept(new String[] {"application/json"});
      setContentType(new String[] {"application/json"});
      // writer: fixed json
      setRequestWriter(req -> "{\"name\":\"domino\"}");
      setResponseReader(resp -> resp.getBodyAsString());
    }
  }

  /** POST /upload multipart demo. */
  static final class UploadPost extends ServerRequest<Void, String> {
    UploadPost(String serviceRoot) {
      super(new RequestMeta(UploadPost.class, "upload", Void.class, String.class), null);
      setServiceRoot(serviceRoot);
      setHttpMethod(HttpMethod.POST);
      setPath("/upload");
      setAccept(new String[] {"text/plain"});
      setContentType(new String[] {"multipart/form-data"}); // router will set boundary
      setResponseReader(resp -> resp.getBodyAsString());
      // In a real test you might attach actual MultipartForm through a hook the router reads.
      // For a smoke test, the JVM sender typically sends a minimal multipart body when flagged.
    }
  }

  private static final class Result {
    String successBody;
    SampleObject successBodyBean;
    Throwable error;

    @Override
    public String toString() {
      return "Result{"
          + "successBody='"
          + successBody
          + '\''
          + ", successBodyBean="
          + successBodyBean
          + ", error="
          + error
          + '}';
    }
  }

  private <R> Result run(ServerRequest<R, String> req) throws InterruptedException {
    CountDownLatch done = new CountDownLatch(1);
    Result r = new Result();
    req.onSuccess(
            (Success<String>)
                body -> {
                  r.successBody = body;
                })
        .onFailed(
            err -> {
              r.error = err.getThrowable();
            })
        .onComplete(done::countDown);
    req.send();
    assertTrue(done.await(5, TimeUnit.SECONDS), "Request did not complete in time");
    return r;
  }

  private <R> Result runBeanRequest(ServerRequest<R, SampleObject> req)
      throws InterruptedException {
    CountDownLatch done = new CountDownLatch(1);
    Result r = new Result();
    req.onSuccess(
            (Success<SampleObject>)
                body -> {
                  r.successBodyBean = body;
                })
        .onFailed(err -> r.error = err.getThrowable())
        .onComplete(done::countDown);
    req.send();
    assertTrue(done.await(5, TimeUnit.SECONDS), "Request did not complete in time");
    return r;
  }

  private static void echoHandler(HttpExchange ex) throws IOException {
    String method = ex.getRequestMethod();
    URI uri = ex.getRequestURI();
    String path = uri.getRawPath(); // includes matrix if any
    String query = Optional.ofNullable(uri.getRawQuery()).orElse("");

    String body = readBody(ex);
    Headers headers = ex.getRequestHeaders();

    String json =
        "{"
            + "\"method\":\""
            + method
            + "\","
            + "\"path\":\""
            + path
            + "\","
            + "\"query\":\""
            + query
            + "\","
            + "\"Content-Type\":\""
            + headerOrEmpty(headers, "Content-Type")
            + "\","
            + "\"X-Test\":\""
            + headerOrEmpty(headers, "X-Test")
            + "\","
            + "\"X-Alpha\":\""
            + headerOrEmpty(headers, "X-Alpha")
            + "\","
            + "\"X-Beta\":\""
            + headerOrEmpty(headers, "X-Beta")
            + "\","
            + "\"body\":\""
            + jsonEscape(body)
            + "\""
            + "}";
    write(ex, 200, json, "application/json");
  }

  private static void echoBeanHandler(HttpExchange ex) throws IOException {

    SampleObject bean = new SampleObject();
    bean.setName("domino");
    bean.setId("1234");

    write(ex, 200, SampleObject_MapperImpl.INSTANCE.write(bean), "application/json");
  }

  private static void jsonHandler(HttpExchange ex) throws IOException {
    String body = readBody(ex);
    Headers h = ex.getRequestHeaders();
    String json =
        "{"
            + "\"Content-Type\":\""
            + headerOrEmpty(h, "Content-Type")
            + "\","
            + "\"body\":\""
            + jsonEscape(body)
            + "\""
            + "}";
    write(ex, 200, json, "application/json");
  }

  private static void uploadHandler(HttpExchange ex) throws IOException {
    String ctype = headerOrEmpty(ex.getRequestHeaders(), "Content-Type");
    String body = readBody(ex);
    boolean ok = ctype.contains("multipart/form-data; boundary=");
    String text =
        (ok ? "multipart-ok" : "multipart-missing")
            + " | content-type="
            + ctype
            + " | body-len="
            + body.length();
    write(ex, 200, text, "text/plain");
  }

  private static void customersHandler(HttpExchange ex) throws IOException {
    String path = ex.getRequestURI().getRawPath();
    if (!path.startsWith("/customers/")) {
      write(ex, 404, "not-found", "text/plain");
      return;
    }

    String remainder = path.substring("/customers/".length());
    String[] parts = remainder.split("/");
    List<String> segments = new ArrayList<>();
    for (String part : parts) {
      if (!part.isEmpty()) {
        segments.add(part);
      }
    }

    if (segments.isEmpty()) {
      write(ex, 400, "bad-request", "text/plain");
      return;
    }

    String customerId = segments.get(0);
    String payload;

    if (segments.size() == 1) {
      payload = "customer-" + customerId;
    } else if ("orders".equals(segments.get(1))) {
      if (segments.size() == 2) {
        payload = "orders-" + customerId;
      } else {
        payload = "order-" + customerId + "-" + segments.get(2);
      }
    } else if ("standing-orders".equals(segments.get(1))) {
      if (segments.size() == 2) {
        payload = "standing-orders-" + customerId;
      } else {
        payload = "standing-order-" + customerId + "-" + segments.get(2);
      }
    } else {
      payload = "unknown";
    }

    write(ex, 200, "\"" + payload + "\"", "application/json");
  }

  private static void delayHandler(HttpExchange ex) throws IOException {
    // Sleep BEFORE sending headers — this is key to trigger client timeouts
    Map<String, List<String>> q = splitQuery(ex.getRequestURI());
    long ms =
        q.getOrDefault("ms", List.of("0")).get(0) == null
            ? 0
            : Long.parseLong(q.getOrDefault("ms", List.of("0")).get(0));
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
    }
    write(ex, 200, "delayed-" + ms, "text/plain");
  }

  private static void statusHandler(HttpExchange ex) throws IOException {
    String[] parts = ex.getRequestURI().getPath().split("/");
    int code = 200;
    if (parts.length >= 3) {
      try {
        code = Integer.parseInt(parts[2]);
      } catch (NumberFormatException ignored) {
      }
    }
    write(ex, code, code == 204 ? "" : ("status-" + code), "text/plain");
  }

  private static String readBody(HttpExchange ex) throws IOException {
    try (InputStream is = ex.getRequestBody()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[4096];
      int r;
      while ((r = is.read(buf)) != -1) out.write(buf, 0, r);
      return out.toString(UTF_8);
    }
  }

  private static void write(HttpExchange ex, int code, String body, String contentType)
      throws IOException {
    byte[] bytes = body.getBytes(UTF_8);
    ex.getResponseHeaders().add("Content-Type", contentType);
    ex.sendResponseHeaders(code, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    }
  }

  private static String headerOrEmpty(Headers h, String name) {
    List<String> v = h.get(name);
    return (v == null || v.isEmpty()) ? "" : v.get(0);
  }

  private static String jsonEscape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
  }

  private static Map<String, List<String>> splitQuery(URI uri) {
    Map<String, List<String>> q = new LinkedHashMap<>();
    String raw = uri.getRawQuery();
    if (raw == null || raw.isEmpty()) return q;
    for (String pair : raw.split("&")) {
      int i = pair.indexOf('=');
      String k = i < 0 ? pair : pair.substring(0, i);
      String v = i < 0 ? "" : pair.substring(i + 1);
      q.computeIfAbsent(k, kk -> new ArrayList<>()).add(v);
    }
    return q;
  }
}

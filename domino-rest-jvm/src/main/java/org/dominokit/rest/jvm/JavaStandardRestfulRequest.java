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
package org.dominokit.rest.jvm;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.dominokit.rest.shared.BaseRestfulRequest;
import org.dominokit.rest.shared.GwtIncompatible;
import org.dominokit.rest.shared.MultipartForm;
import org.dominokit.rest.shared.RestfulRequest;

/** Java implementation for {@link RestfulRequest} using the standard JDK HTTP Client. */
@GwtIncompatible
public class JavaStandardRestfulRequest extends BaseRestfulRequest {

  private final String method;

  private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
  private final Map<String, String> headers = new LinkedHashMap<>();

  private final HttpClient httpClient;
  private volatile int timeoutMillis = 0; // 0 = not set

  private final AtomicReference<CompletableFuture<HttpResponse<byte[]>>> inFlight =
      new AtomicReference<>();

  public JavaStandardRestfulRequest(String uri, String method) {
    super(uri, method);
    this.method = method == null ? "GET" : method.toUpperCase(Locale.ROOT);
    this.httpClient =
        HttpClient.newBuilder()
            .version(Version.HTTP_1_1) // keep conservative default; switch to 2 if you prefer
            .followRedirects(Redirect.NORMAL)
            .build();
  }

  @Override
  public RestfulRequest putHeader(String key, String value) {
    headers.put(key, value);
    return this;
  }

  @Override
  public RestfulRequest putHeaders(Map<String, String> headers) {
    if (nonNull(headers)) {
      headers.forEach(this::putHeader);
    }
    return this;
  }

  @Override
  public Map<String, String> getHeaders() {
    return new LinkedHashMap<>(headers);
  }

  @Override
  public RestfulRequest timeout(int timeout) {
    this.timeoutMillis = timeout;
    return super.timeout(timeout);
  }

  @Override
  public void setWithCredentials(boolean withCredentials) {
    // Not applicable for JDK client (browser concept); noop
  }

  @Override
  public RestfulRequest setResponseType(String responseType) {
    // Not applicable for JDK client; noop
    return this;
  }

  @Override
  public void sendForm(Map<String, String> formData) {
    putHeader("Content-Type", "application/x-www-form-urlencoded");
    String body =
        formData.entrySet().stream()
            .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
            .collect(joining("&"));
    send(body);
  }

  @Override
  public void sendJson(String json) {
    putHeader("Content-Type", "application/json");
    send(json);
  }

  @Override
  public void sendMultipartForm(MultipartForm multipartForm) {
    // Build multipart body manually
    String boundary = "----DominoRestBoundary" + UUID.randomUUID();
    putHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
    try {
      byte[] bytes = buildMultipartBody(multipartForm, boundary);
      send(bytes);
    } catch (IOException ex) {
      errorHandler.onError(ex);
    }
  }

  @Override
  public void send(String data) {
    send(data == null ? new byte[0] : data.getBytes(UTF_8));
  }

  private void send(byte[] data) {
    try {
      HttpRequest.Builder b = startBuilderWithUriAndHeaders();
      if (allowsRequestBody(method)) {
        b.method(method, HttpRequest.BodyPublishers.ofByteArray(data));
      } else {
        // If a body was given but method typically doesn't send one, we still try to comply.
        // Many servers ignore a body on GET/DELETE.
        b.method(method, HttpRequest.BodyPublishers.ofByteArray(data));
      }
      applyTimeout(b);

      HttpRequest httpReq = b.build();
      CompletableFuture<HttpResponse<byte[]>> fut =
          httpClient.sendAsync(httpReq, HttpResponse.BodyHandlers.ofByteArray());
      inFlight.set(fut);
      fut.whenComplete(
          (resp, err) -> {
            inFlight.compareAndSet(fut, null);
            if (err != null) {
              errorHandler.onError(
                  err instanceof HttpTimeoutException ? err : unwrapCompletion(err));
            } else {
              successHandler.onResponseReceived(new StandardJavaResponse(resp));
            }
          });
    } catch (Throwable t) {
      errorHandler.onError(t);
    }
  }

  @Override
  public void send() {
    try {
      HttpRequest.Builder b = startBuilderWithUriAndHeaders();
      if (usesRequestBody(method)) {
        // Methods like POST/PUT/PATCH without body → send empty body
        b.method(method, HttpRequest.BodyPublishers.noBody());
      } else {
        b.method(method, HttpRequest.BodyPublishers.noBody());
      }
      applyTimeout(b);

      HttpRequest httpReq = b.build();
      CompletableFuture<HttpResponse<byte[]>> fut =
          httpClient.sendAsync(httpReq, HttpResponse.BodyHandlers.ofByteArray());
      inFlight.set(fut);
      fut.whenComplete(
          (resp, err) -> {
            inFlight.compareAndSet(fut, null);
            if (err != null) {
              errorHandler.onError(unwrapCompletion(err));
            } else {
              successHandler.onResponseReceived(new StandardJavaResponse(resp));
            }
          });
    } catch (Throwable t) {
      errorHandler.onError(t);
    }
  }

  @Override
  public void abort() {
    CompletableFuture<HttpResponse<byte[]>> fut = inFlight.getAndSet(null);
    if (fut != null) {
      fut.cancel(true);
    }
  }

  private HttpRequest.Builder startBuilderWithUriAndHeaders() {
    URI finalUri = URI.create(getUri());
    HttpRequest.Builder b = HttpRequest.newBuilder(finalUri);
    // headers
    headers.forEach(b::header);
    return b;
  }

  private void applyTimeout(HttpRequest.Builder b) {
    if (timeoutMillis > 0) {
      b.timeout(Duration.ofMillis(timeoutMillis));
    }
  }

  private static Throwable unwrapCompletion(Throwable t) {
    // Unwrap CompletionException/ExecutionException when possible
    if (t.getCause() != null) return t.getCause();
    return t;
  }

  private static boolean usesRequestBody(String method) {
    String m = method.toUpperCase(Locale.ROOT);
    return "POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m);
  }

  private static boolean allowsRequestBody(String method) {
    return true;
  }

  private static String urlEncode(String s) {
    try {
      return URLEncoder.encode(s, UTF_8.name());
    } catch (Exception e) {
      return s;
    }
  }

  private static byte[] buildMultipartBody(MultipartForm form, String boundary) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    String dashBoundary = "--" + boundary;

    for (MultipartForm.TextMultipart t : form.getTextMultiParts()) {
      out.write((dashBoundary + "\r\n").getBytes(UTF_8));
      out.write(
          ("Content-Disposition: form-data; name=\"" + t.name() + "\"\r\n\r\n").getBytes(UTF_8));
      out.write((t.value() == null ? "" : t.value()).getBytes(UTF_8));
      out.write("\r\n".getBytes(UTF_8));
    }

    for (MultipartForm.FileMultipart f : form.getFileMultiParts()) {
      out.write((dashBoundary + "\r\n").getBytes(UTF_8));
      String filename = f.name(); // you can add a separate filename() if available
      String ctype = f.contentType() == null ? "application/octet-stream" : f.contentType();
      out.write(
          ("Content-Disposition: form-data; name=\""
                  + f.name()
                  + "\"; filename=\""
                  + filename
                  + "\"\r\n")
              .getBytes(UTF_8));
      out.write(("Content-Type: " + ctype + "\r\n\r\n").getBytes(UTF_8));
      out.write(f.value()); // raw bytes
      out.write("\r\n".getBytes(UTF_8));
    }

    out.write((dashBoundary + "--\r\n").getBytes(UTF_8));
    return out.toByteArray();
  }
}

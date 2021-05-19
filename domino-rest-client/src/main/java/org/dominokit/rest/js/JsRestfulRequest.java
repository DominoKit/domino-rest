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
package org.dominokit.rest.js;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import elemental2.core.ArrayBuffer;
import elemental2.core.TypedArray;
import elemental2.core.Uint8Array;
import elemental2.dom.Blob;
import elemental2.dom.BlobPropertyBag;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dominokit.rest.shared.BaseRestfulRequest;
import org.dominokit.rest.shared.MultipartForm;
import org.dominokit.rest.shared.RestfulRequest;
import org.dominokit.rest.shared.request.RequestTimeoutException;
import org.gwtproject.timer.client.Timer;

/** JS implementation for {@link RestfulRequest} that uses {@link XMLHttpRequest} */
public class JsRestfulRequest extends BaseRestfulRequest {

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_PDF = "application/pdf";
  private final XMLHttpRequest request;
  private final Map<String, List<String>> params = new LinkedHashMap<>();
  private final Map<String, String> headers = new LinkedHashMap<>();
  private final Timer timer =
      new Timer() {
        @Override
        public void run() {
          fireOnTimeout();
        }
      };

  public JsRestfulRequest(String uri, String method) {
    super(uri, method);
    request = new XMLHttpRequest();
    parseUri(uri);
  }

  private void parseUri(String uri) {
    if (uri.contains("?")) {
      String[] uriParts = uri.split("\\?");
      addQueryString(uriParts[1]);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected String paramsAsString() {
    return params.entrySet().stream().map(this::entryAsString).collect(joining("&"));
  }

  private String entryAsString(Map.Entry<String, List<String>> paramValuePair) {
    return paramValuePair.getValue().stream()
        .map(v -> paramValuePair.getKey() + "=" + v)
        .collect(joining("&"));
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest addQueryParam(String key, String value) {
    if (!params.containsKey(key)) params.put(key, new ArrayList<>());
    params.get(key).add(value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest setQueryParam(String key, String value) {
    params.put(key, new ArrayList<>());
    addQueryParam(key, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest putHeader(String key, String value) {
    if (CONTENT_TYPE.equalsIgnoreCase(key)) {
      if (APPLICATION_PDF.equalsIgnoreCase(value)) {
        request.responseType = "arraybuffer";
      }
    }
    headers.put(key, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest putHeaders(Map<String, String> headers) {
    if (nonNull(headers)) {
      headers.forEach(this::putHeader);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest putParameters(Map<String, List<String>> parameters) {
    if (nonNull(parameters) && !parameters.isEmpty()) {
      parameters.forEach((key, values) -> values.forEach(value -> addQueryParam(key, value)));
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getHeaders() {
    return headers;
  }

  /** {@inheritDoc} */
  @Override
  public void sendForm(Map<String, String> formData) {
    setContentType("application/x-www-form-urlencoded");
    send(
        formData.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(joining("&")));
  }

  /** {@inheritDoc} */
  @Override
  public void sendJson(String json) {
    setContentType("application/json");
    send(json);
  }

  /** {@inheritDoc} */
  @Override
  public void sendMultipartForm(MultipartForm multipartForm) {
    initRequest();
    FormData data = new FormData();
    for (MultipartForm.TextMultipart textMultipart : multipartForm.getTextMultiParts()) {
      BlobPropertyBag blobPropertyBag = BlobPropertyBag.create();
      blobPropertyBag.setType(textMultipart.contentType());
      Blob blob =
          new Blob(
              new Blob.ConstructorBlobPartsArrayUnionType[] {
                Blob.ConstructorBlobPartsArrayUnionType.of(textMultipart.value())
              },
              blobPropertyBag);
      if (textMultipart.fileName().isPresent()) {
        data.append(textMultipart.name(), blob, textMultipart.fileName().get());
      } else {
        data.append(textMultipart.name(), blob);
      }
    }
    for (MultipartForm.FileMultipart fileMultipart : multipartForm.getFileMultiParts()) {
      ArrayBuffer arrayBuffer = new ArrayBuffer(fileMultipart.value().length);
      Uint8Array buffer = new Uint8Array(arrayBuffer);
      buffer.set(TypedArray.SetArrayUnionType.of(fileMultipart.value()));
      BlobPropertyBag options = BlobPropertyBag.create();
      options.setType(fileMultipart.contentType());
      Blob blob =
          new Blob(
              new Blob.ConstructorBlobPartsArrayUnionType[] {
                Blob.ConstructorBlobPartsArrayUnionType.of(buffer)
              },
              options);
      if (fileMultipart.fileName().isPresent()) {
        data.append(fileMultipart.name(), blob, fileMultipart.fileName().get());
      } else {
        data.append(fileMultipart.name(), blob);
      }
    }
    request.send(data);
  }

  /** {@inheritDoc} */
  @Override
  public void send(String data) {
    initRequest();
    request.send(data);
  }

  /** {@inheritDoc} */
  @Override
  public void send() {
    initRequest();
    request.send();
  }

  /** {@inheritDoc} */
  @Override
  public void abort() {
    request.onreadystatechange = p0 -> null;
    request.abort();
  }

  /** {@inheritDoc} */
  @Override
  public void setWithCredentials(boolean withCredentials) {
    request.withCredentials = withCredentials;
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest setResponseType(String responseType) {
    request.responseType = responseType;
    return this;
  }

  private void setContentType(String contentType) {
    headers.put(CONTENT_TYPE, contentType);
  }

  private void initRequest() {
    String url = getUri();
    request.open(getMethod(), url);
    setHeaders();
    request.onreadystatechange =
        xhr -> {
          if (request.readyState == XMLHttpRequest.DONE) {
            request.onreadystatechange = p0 -> null;
            timer.cancel();
            successHandler.onResponseReceived(new JsResponse(request));
          }
          return null;
        };
    if (getTimeout() > 0) {
      timer.schedule(getTimeout());
    }
  }

  private void fireOnTimeout() {
    timer.cancel();
    request.onreadystatechange = p0 -> null;
    request.abort();
    errorHandler.onError(new RequestTimeoutException());
  }

  private void setHeaders() {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      request.setRequestHeader(entry.getKey(), entry.getValue());
    }
  }
}

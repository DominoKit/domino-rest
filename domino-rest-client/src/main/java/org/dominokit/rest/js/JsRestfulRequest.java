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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dominokit.rest.shared.BaseRestfulRequest;
import org.dominokit.rest.shared.RestfulRequest;
import org.dominokit.rest.shared.request.RequestTimeoutException;
import org.gwtproject.timer.client.Timer;
import org.gwtproject.xhr.client.XMLHttpRequest;

/** JS implementation for {@link RestfulRequest} that uses {@link XMLHttpRequest} */
public class JsRestfulRequest extends BaseRestfulRequest {

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_PDF = "application/pdf";
  private XMLHttpRequest request;
  private Map<String, List<String>> params = new LinkedHashMap<>();
  private Map<String, String> headers = new LinkedHashMap<>();
  private final Timer timer =
      new Timer() {
        @Override
        public void run() {
          fireOnTimeout();
        }
      };

  public JsRestfulRequest(String uri, String method) {
    super(uri, method);
    request = XMLHttpRequest.create();
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
        request.setResponseType(XMLHttpRequest.ResponseType.ArrayBuffer);
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
    request.clearOnReadyStateChange();
    request.abort();
  }

  /** {@inheritDoc} */
  @Override
  public void setWithCredentials(boolean withCredentials) {
    request.setWithCredentials(withCredentials);
  }

  /** {@inheritDoc} */
  @Override
  public RestfulRequest setResponseType(String responseType) {
    request.setResponseType(responseType);
    return this;
  }

  private void setContentType(String contentType) {
    headers.put(CONTENT_TYPE, contentType);
  }

  private void initRequest() {
    String url = getUri();
    request.open(getMethod(), url);
    setHeaders();
    request.setOnReadyStateChange(
        xhr -> {
          if (xhr.getReadyState() == XMLHttpRequest.DONE) {
            xhr.clearOnReadyStateChange();
            timer.cancel();
            successHandler.onResponseReceived(new JsResponse(xhr));
          }
        });
    if (getTimeout() > 0) {
      timer.schedule(getTimeout());
    }
  }

  private void fireOnTimeout() {
    timer.cancel();
    request.clearOnReadyStateChange();
    request.abort();
    errorHandler.onError(new RequestTimeoutException());
  }

  private void setHeaders() {
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      request.setRequestHeader(entry.getKey(), entry.getValue());
    }
  }
}

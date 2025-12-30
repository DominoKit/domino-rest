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

import elemental2.core.ArrayBuffer;
import elemental2.core.Int8Array;
import elemental2.dom.XMLHttpRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jsinterop.base.Js;
import org.dominokit.rest.shared.Response;

/** JS implementation for the {@link Response} */
public class JsResponse implements Response {

  private final XMLHttpRequest request;
  private Object responseBean;

  JsResponse(XMLHttpRequest request) {
    this.request = request;
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getHeader(String header) {
    return Collections.singletonList(request.getResponseHeader(header));
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, List<String>> getHeaders() {
    String allResponseHeaders = request.getAllResponseHeaders();
    String[] headers = allResponseHeaders.split("\r\n");
    return Stream.of(headers)
        .filter(header -> !header.isEmpty())
        .map(header -> header.split(":", 2))
        .collect(
            Collectors.toMap(
                header -> header[0], header -> Collections.singletonList(header[1].trim())));
  }

  /** {@inheritDoc} */
  @Override
  public int getStatusCode() {
    return request.status;
  }

  /** {@inheritDoc} */
  @Override
  public String getStatusText() {
    return request.statusText;
  }

  /** {@inheritDoc} */
  @Override
  public String getBodyAsString() {
    return request.responseText;
  }

  /**
   * Reads the response content as {@link ArrayBuffer}, this is useful when setting the response
   * type to {@code arraybuffer}
   *
   * @return the content of the response as array buffer
   */
  public ArrayBuffer getResponseArrayBuffer() {
    return Js.cast(request.response);
  }

  /** @return the {@code XMLHttpRequest} associated with this response. */
  public XMLHttpRequest getRequest() {
    return request;
  }

  @Override
  public byte[] getBodyAsBytes() {
    return toByteArray(getResponseArrayBuffer());
  }

  public static byte[] toByteArray(ArrayBuffer buffer) {
    Int8Array view = new Int8Array(buffer); // signed bytes [-128,127]
    int len = (int) view.length;
    byte[] out = new byte[len];
    for (int i = 0; i < len; i++) {
      out[i] = Js.uncheckedCast(view.getAt(i)); // getAt returns a JS number
    }
    return out;
  }

  /** Copy a slice (offset/length in bytes) into a Java byte[]. */
  public static byte[] toByteArray(ArrayBuffer buffer, int byteOffset, int length) {
    Int8Array view = new Int8Array(buffer, byteOffset, length);
    int len = (int) view.length;
    byte[] out = new byte[len];
    for (int i = 0; i < len; i++) {
      out[i] = Js.uncheckedCast(view.getAt(i));
    }
    return out;
  }

  public void setResponseBean(Object responseBean) {
    this.responseBean = responseBean;
  }

  @Override
  public Optional<Object> getBean() {
    return Optional.ofNullable(responseBean);
  }

  @Override
  public void setBean(Object bean) {
    if (nonNull(this.responseBean)) {
      throw new IllegalStateException("The response bean has already been set");
    }
    this.responseBean = bean;
  }
}

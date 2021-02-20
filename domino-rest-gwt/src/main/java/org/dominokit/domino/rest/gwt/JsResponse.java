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
package org.dominokit.domino.rest.gwt;

import elemental2.core.ArrayBuffer;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jsinterop.base.Js;
import org.dominokit.domino.rest.shared.Response;
import org.gwtproject.xhr.client.XMLHttpRequest;

/** JS implementation for the {@link Response} */
public class JsResponse implements Response {

  private final XMLHttpRequest request;

  JsResponse(XMLHttpRequest request) {
    this.request = request;
  }

  /** {@inheritDoc} */
  @Override
  public String getHeader(String header) {
    return request.getResponseHeader(header);
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getHeaders() {
    String allResponseHeaders = request.getAllResponseHeaders();
    String[] headers = allResponseHeaders.split("\r\n");
    return Stream.of(headers)
        .filter(header -> !header.isEmpty())
        .map(header -> header.split(":", 2))
        .collect(Collectors.toMap(header -> header[0], header -> header[1].trim()));
  }

  /** {@inheritDoc} */
  @Override
  public int getStatusCode() {
    return request.getStatus();
  }

  /** {@inheritDoc} */
  @Override
  public String getStatusText() {
    return request.getStatusText();
  }

  /** {@inheritDoc} */
  @Override
  public String getBodyAsString() {
    return request.getResponseText();
  }

  /**
   * Reads the response content as {@link ArrayBuffer}, this is useful when setting the response
   * type to {@code arraybuffer}
   *
   * @return the content of the response as array buffer
   */
  public ArrayBuffer getResponseArrayBuffer() {
    return Js.cast(request.getResponseArrayBuffer());
  }
}

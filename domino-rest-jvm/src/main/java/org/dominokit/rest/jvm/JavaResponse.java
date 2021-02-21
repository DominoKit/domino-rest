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

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import java.util.Map;
import java.util.stream.Collectors;
import org.dominokit.rest.shared.GwtIncompatible;
import org.dominokit.rest.shared.Response;

/** Java implementation for the {@link Response} */
@GwtIncompatible
public class JavaResponse implements Response {

  private final HttpResponse<Buffer> response;

  JavaResponse(HttpResponse<Buffer> response) {
    this.response = response;
  }

  /** {@inheritDoc} */
  @Override
  public String getHeader(String header) {
    return response.getHeader(header);
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getHeaders() {
    return response.headers().entries().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /** {@inheritDoc} */
  @Override
  public int getStatusCode() {
    return response.statusCode();
  }

  /** {@inheritDoc} */
  @Override
  public String getStatusText() {
    return response.statusMessage();
  }

  /** {@inheritDoc} */
  @Override
  public String getBodyAsString() {
    return response.bodyAsString();
  }
}

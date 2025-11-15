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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public List<String> getHeader(String header) {
    return Collections.singletonList(response.getHeader(header));
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, List<String>> getHeaders() {
    Map<String, List<String>> map = new HashMap<>();
    response.headers().entries().stream()
        .filter(
            stringStringEntry ->
                map.put(
                        stringStringEntry.getKey(),
                        Collections.singletonList(stringStringEntry.getValue()))
                    != null)
        .forEach(
            stringStringEntry -> {
              throw new IllegalStateException("Duplicate key");
            });
    return map;
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

  @Override
  public byte[] getBodyAsBytes() {
    return new byte[0];
  }
}

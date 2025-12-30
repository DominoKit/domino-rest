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

import static java.util.Objects.nonNull;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dominokit.rest.shared.Response;

/** Simple adapter over JDK HttpResponse<byte[]> to your shared Response. */
public class StandardJavaResponse implements Response {

  private final HttpResponse<byte[]> delegate;
  private Object responseBean;

  public StandardJavaResponse(HttpResponse<byte[]> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getStatusCode() {
    return delegate.statusCode();
  }

  @Override
  public String getStatusText() {
    // JDK HttpResponse doesn't carry reason phrase; return empty or synthesize
    return "";
  }

  @Override
  public String getBodyAsString() {
    return delegate.body() == null ? "" : new String(delegate.body());
  }

  @Override
  public byte[] getBodyAsBytes() {
    return delegate.body();
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return delegate.headers().map();
  }

  @Override
  public List<String> getHeader(String header) {
    return delegate.headers().allValues(header);
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

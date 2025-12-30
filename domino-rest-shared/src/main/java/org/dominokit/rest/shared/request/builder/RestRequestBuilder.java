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
package org.dominokit.rest.shared.request.builder;

import static java.util.Objects.isNull;

import java.util.Optional;
import org.dominokit.rest.shared.request.RequestMeta;
import org.dominokit.rest.shared.request.RequestWriter;
import org.dominokit.rest.shared.request.ResponseReader;
import org.dominokit.rest.shared.request.ServerRequest;

/**
 * A builder for creating and configuring REST requests.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public class RestRequestBuilder<R, S>
    implements HasMethod<R, S>, HasPath<R, S>, Consumes<R, S>, Produces<R, S> {

  private String key;
  private Class<R> requestClass;
  private Class<S> responseClass;
  private String method;
  private String consumes;
  private String produce;
  private String path;
  private String serviceRoot = "";
  private ResponseReader<S> responseReader = response -> null;
  private RequestWriter<R> requestWriter = request -> null;

  /**
   * Creates a new instance of {@link RestRequestBuilder}.
   *
   * @param requestClass the request class
   * @param responseClass the response class
   * @param key a key to identify the request
   * @param <R> the request type
   * @param <S> the response type
   * @return the builder instance
   */
  public static <R, S> HasMethod<R, S> of(
      Class<R> requestClass, Class<S> responseClass, String key) {
    RestRequestBuilder<R, S> builder = new RestRequestBuilder<>();
    builder.key = key;
    builder.requestClass = requestClass;
    builder.responseClass = responseClass;
    return builder;
  }

  /** {@inheritDoc} */
  @Override
  public HasPath<R, S> withMethod(String method) {
    this.method = method;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public Consumes<R, S> withPath(String path) {
    this.path = path;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public Produces<R, S> accepts(String consumes) {
    this.consumes = consumes;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RestRequestBuilder<R, S> produces(String produces) {
    this.produce = produces;
    return this;
  }

  /**
   * Sets the service root.
   *
   * @param serviceRoot the service root
   * @return same instance to support builder pattern
   */
  public RestRequestBuilder<R, S> withServiceRoot(String serviceRoot) {
    this.serviceRoot = serviceRoot;
    return this;
  }

  /**
   * Sets the response reader.
   *
   * @param responseReader the response reader
   * @return same instance to support builder pattern
   */
  public RestRequestBuilder<R, S> withResponseReader(ResponseReader<S> responseReader) {
    this.responseReader = responseReader;
    return this;
  }

  /**
   * Sets the request writer.
   *
   * @param requestWriter the request writer
   * @return same instance to support builder pattern
   */
  public RestRequestBuilder<R, S> withRequestWriter(RequestWriter<R> requestWriter) {
    this.requestWriter = requestWriter;
    return this;
  }

  /**
   * Builds the request.
   *
   * @return the created {@link ServerRequest}
   */
  public ServerRequest<R, S> build() {
    return build(null);
  }

  /**
   * Builds the request with a request bean.
   *
   * @param requestBean the request bean
   * @return the created {@link ServerRequest}
   */
  public ServerRequest<R, S> build(R requestBean) {
    ServerRequest<R, S> request =
        new GenericRequest<>(
            new RequestMeta(RestRequestBuilder.class, key, requestClass, responseClass),
            requestBean);
    request.setHttpMethod(this.method);
    request.setAccept(new String[] {this.produce});
    request.setContentType(new String[] {this.consumes});
    request.setPath(this.path);
    request.setServiceRoot(isNull(this.serviceRoot) ? "" : this.serviceRoot);
    Optional.ofNullable(this.responseReader).ifPresent(request::setResponseReader);
    Optional.ofNullable(this.requestWriter).ifPresent(request::setRequestWriter);

    return request;
  }
}

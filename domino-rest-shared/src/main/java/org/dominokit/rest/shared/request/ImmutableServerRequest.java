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
package org.dominokit.rest.shared.request;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Immutable wrapper around a ServerRequest that provides read-only access to the request
 * properties. This class implements IServerRequest and delegates all method calls to the wrapped
 * ServerRequest instance, ensuring that all returned collections and arrays are immutable or
 * defensive copies.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public class ImmutableServerRequest<R, S> implements IServerRequest<R, S> {

  private final ServerRequest<R, S> delegate;

  /**
   * Creates a new ImmutableServerRequest wrapping the provided ServerRequest.
   *
   * @param delegate the ServerRequest to wrap (must not be null)
   * @throws NullPointerException if delegate is null
   */
  public ImmutableServerRequest(ServerRequest<R, S> delegate) {
    this.delegate = requireNonNull(delegate, "ServerRequest delegate cannot be null");
  }

  /**
   * Creates an immutable view of the provided ServerRequest.
   *
   * @param request the ServerRequest to wrap
   * @param <R> the request type
   * @param <S> the response type
   * @return an ImmutableServerRequest wrapping the provided request
   */
  public static <R, S> ImmutableServerRequest<R, S> of(ServerRequest<R, S> request) {
    return new ImmutableServerRequest<>(request);
  }

  /** {@inheritDoc} */
  @Override
  public RequestMeta getMeta() {
    return delegate.getMeta();
  }

  /** {@inheritDoc} */
  @Override
  public RequestRestSender<R, S> getSender() {
    return delegate.getSender();
  }

  /** {@inheritDoc} */
  @Override
  public R requestBean() {
    return delegate.requestBean();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<ServerRequest.WithCredentialsRequest> getWithCredentialsRequest() {
    return delegate.getWithCredentialsRequest();
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of headers
   */
  @Override
  public Map<String, String> headers() {
    return Collections.unmodifiableMap(delegate.headers());
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of query parameters with unmodifiable lists
   */
  @Override
  public Map<String, List<String>> queryParameters() {
    return toUnmodifiableMapOfLists(delegate.queryParameters());
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of path parameters
   */
  @Override
  public Map<String, String> pathParameters() {
    return Collections.unmodifiableMap(delegate.pathParameters());
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of matrix parameters with unmodifiable lists
   */
  @Override
  public Map<String, List<String>> matrixParameters() {
    return toUnmodifiableMapOfLists(delegate.matrixParameters());
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of fragment parameters
   */
  @Override
  public Map<String, String> fragmentParameters() {
    return Collections.unmodifiableMap(delegate.fragmentParameters());
  }

  /** {@inheritDoc} */
  @Override
  public String getMetaParameter(String key) {
    return delegate.getMetaParameter(key);
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of meta parameters
   */
  @Override
  public Map<String, String> getMetaParameters() {
    return Collections.unmodifiableMap(delegate.getMetaParameters());
  }

  /**
   * {@inheritDoc}
   *
   * @return an unmodifiable map of all request parameters with unmodifiable lists
   */
  @Override
  public Map<String, List<String>> getRequestParameters() {
    return toUnmodifiableMapOfLists(delegate.getRequestParameters());
  }

  /** {@inheritDoc} */
  @Override
  public String getHttpMethod() {
    return delegate.getHttpMethod();
  }

  /**
   * {@inheritDoc}
   *
   * @return a cloned array of success codes
   */
  @Override
  public Integer[] getSuccessCodes() {
    Integer[] codes = delegate.getSuccessCodes();
    return codes != null ? Arrays.copyOf(codes, codes.length) : null;
  }

  /** {@inheritDoc} */
  @Override
  public String getServiceRoot() {
    return delegate.getServiceRoot();
  }

  /** {@inheritDoc} */
  @Override
  public RequestWriter<R> getRequestWriter() {
    return delegate.getRequestWriter();
  }

  /** {@inheritDoc} */
  @Override
  public ResponseReader<S> getResponseReader() {
    return delegate.getResponseReader();
  }

  /** {@inheritDoc} */
  @Override
  public String getPath() {
    return delegate.getPath();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAborted() {
    return delegate.isAborted();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isVoidRequest() {
    return delegate.isVoidRequest();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isVoidResponse() {
    return delegate.isVoidResponse();
  }

  /** {@inheritDoc} */
  @Override
  public String getUrl() {
    return delegate.getUrl();
  }

  @Override
  public String getMatchedUrl() {
    return delegate.getMatchedUrl();
  }

  /** {@inheritDoc} */
  @Override
  public int getTimeout() {
    return delegate.getTimeout();
  }

  /** {@inheritDoc} */
  @Override
  public int getMaxRetries() {
    return delegate.getMaxRetries();
  }

  /** {@inheritDoc} */
  @Override
  public String getResponseType() {
    return delegate.getResponseType();
  }

  /** {@inheritDoc} */
  @Override
  public String emptyOrStringValue(Supplier<?> supplier) {
    return delegate.emptyOrStringValue(supplier);
  }

  /** {@inheritDoc} */
  @Override
  public String formatDate(Supplier<Date> supplier, String pattern) {
    return delegate.formatDate(supplier, pattern);
  }

  /** {@inheritDoc} */
  @Override
  public NullQueryParamStrategy getNullParamStrategy() {
    return delegate.getNullParamStrategy();
  }

  /** {@inheritDoc} */
  @Override
  public boolean isMultipartForm() {
    return delegate.isMultipartForm();
  }

  /**
   * Returns the wrapped ServerRequest instance. Use with caution as it provides mutable access to
   * the underlying request.
   *
   * @return the wrapped ServerRequest
   */
  public ServerRequest<R, S> getDelegate() {
    return delegate;
  }

  /**
   * Helper method to convert a map of lists into an unmodifiable map with unmodifiable lists.
   *
   * @param map the map to convert
   * @return an unmodifiable map with unmodifiable lists
   */
  private static <K, V> Map<K, List<V>> toUnmodifiableMapOfLists(Map<K, List<V>> map) {
    if (map == null || map.isEmpty()) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(
        map.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Collections.unmodifiableList(new ArrayList<>(entry.getValue())))));
  }

  @Override
  public String toString() {
    return "ImmutableServerRequest{" + delegate.toString() + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ImmutableServerRequest<?, ?> that = (ImmutableServerRequest<?, ?>) obj;
    return delegate.equals(that.delegate);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}

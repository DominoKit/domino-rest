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
package org.dominokit.rest.shared;

import java.util.List;
import java.util.Map;
import org.dominokit.rest.RestfulRequestFactory;
import org.dominokit.rest.RestfullRequestContext;

/** A representation of REST request */
public interface RestfulRequest {

  String POST = "POST";
  String GET = "GET";
  String PUT = "PUT";
  String DELETE = "DELETE";
  String HEAD = "HEAD";
  String OPTIONS = "OPTIONS";
  String PATCH = "PATCH";

  /** @return returns the factory of the {@link RestfulRequest} */
  static RestfulRequestFactory factory() {
    return RestfullRequestContext.getFactory();
  }

  /**
   * Creates request with uri and a method
   *
   * @param uri the request uri
   * @param method the request method
   * @return a new request
   */
  static RestfulRequest request(String uri, String method) {
    return factory().request(uri, method);
  }

  /**
   * Creates a POST request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest post(String uri) {
    return factory().post(uri);
  }

  /**
   * Creates a GET request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest get(String uri) {
    return factory().get(uri);
  }

  /**
   * Creates a PUT request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest put(String uri) {
    return factory().put(uri);
  }

  /**
   * Creates a DELETE request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest delete(String uri) {
    return factory().delete(uri);
  }

  /**
   * Creates a HEAD request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest head(String uri) {
    return factory().head(uri);
  }

  /**
   * Creates a OPTIONS request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest options(String uri) {
    return factory().options(uri);
  }

  /**
   * Creates a PATCH request with uri
   *
   * @param uri the request uri
   * @return a new request
   */
  static RestfulRequest patch(String uri) {
    return factory().patch(uri);
  }

  /**
   * Adds query string to the request uri
   *
   * @param queryString the query
   * @return same instance to support builder pattern
   */
  RestfulRequest addQueryString(String queryString);

  /** @return the uri of the request */
  String getUri();

  /**
   * Adds query parameter
   *
   * @param key the key
   * @param value the value
   * @return same instance to support builder pattern
   */
  RestfulRequest addQueryParam(String key, String value);

  /**
   * Adds query parameter with collection of values
   *
   * @param key the key
   * @param values the values
   * @return same instance to support builder pattern
   */
  RestfulRequest addQueryParams(String key, Iterable<String> values);

  /**
   * Adds or replace a query parameter
   *
   * @param key the key
   * @param value the value
   * @return same instance to support builder pattern
   */
  RestfulRequest setQueryParam(String key, String value);

  /** @return the query of the request */
  String getQuery();

  /** @return the path of the request */
  String getPath();

  /** @return the method of the request */
  String getMethod();

  /**
   * Adds new header
   *
   * @param key the key
   * @param value the value
   * @return same instance to support builder pattern
   */
  RestfulRequest putHeader(String key, String value);

  /**
   * Adds a collection of headers
   *
   * @param headers the headers
   * @return same instance to support builder pattern
   */
  RestfulRequest putHeaders(Map<String, String> headers);

  /**
   * Adds a collection of requests parameters
   *
   * @param parameters the parameters
   * @return same instance to support builder pattern
   */
  RestfulRequest putParameters(Map<String, List<String>> parameters);

  /** @return the request headers */
  Map<String, String> getHeaders();

  /**
   * Sets the timeout of the request
   *
   * @param timeout the timeout in milliseconds
   * @return same instance to support builder pattern
   */
  RestfulRequest timeout(int timeout);

  /** @return the timeout of the request */
  int getTimeout();

  /**
   * Sends the request with content type {@code application/x-www-form-urlencoded}
   *
   * @param formData the body of the request
   */
  void sendForm(Map<String, String> formData);

  /**
   * Sends the request with content type {@code application/json}
   *
   * @param json the body of the request
   */
  void sendJson(String json);

  /**
   * Sends the request with content type {@code multipart/form-data}
   *
   * @param multipartForm the body of the request
   */
  void sendMultipartForm(MultipartForm multipartForm);

  /**
   * Sends the request with the default content type
   *
   * @param data the body of the request
   */
  void send(String data);

  /** Sends a request with no body */
  void send();

  /** Abort the request */
  void abort();

  /**
   * Sets with credentials support for the request
   *
   * @param withCredentials true if it is supported, false otherwise
   */
  void setWithCredentials(boolean withCredentials);

  /**
   * Sets the type of the response
   *
   * @param responseType the response type
   * @return same instance to support builder pattern
   */
  RestfulRequest setResponseType(String responseType);

  /**
   * Sets a success handler to be called if the request succeed
   *
   * @param successHandler the handler
   * @return same instance to support builder pattern
   */
  RestfulRequest onSuccess(SuccessHandler successHandler);

  /**
   * Sets an error handler to be called if the request failed
   *
   * @param errorHandler the handler
   * @return same instance to support builder pattern
   */
  RestfulRequest onError(ErrorHandler errorHandler);

  @FunctionalInterface
  interface SuccessHandler {
    void onResponseReceived(Response response);
  }

  @FunctionalInterface
  interface ErrorHandler {
    void onError(Throwable throwable);
  }
}

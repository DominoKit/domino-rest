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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Read-only interface representing the public API of a server request. This interface provides
 * access to request properties and parameters without allowing modifications.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public interface IServerRequest<R, S> {

  /**
   * @return the request metadata
   */
  RequestMeta getMeta();

  /**
   * @return the rest sender associated with this request
   */
  RequestRestSender<R, S> getSender();

  /**
   * @return the request bean of the request
   */
  R requestBean();

  /**
   * @return Optional if the with credentials is supported by this request
   */
  Optional<ServerRequest.WithCredentialsRequest> getWithCredentialsRequest();

  /**
   * @return new map containing all headers defined in the request
   */
  Map<String, String> headers();

  /**
   * @return new map containing all query parameters defined in the request
   */
  Map<String, List<String>> queryParameters();

  /**
   * @return new map containing all path parameters defined in the request
   */
  Map<String, String> pathParameters();

  /**
   * @return new map containing all matrix parameters defined in the request
   */
  Map<String, List<String>> matrixParameters();

  /**
   * @return new map containing all fragment parameters defined in the request
   */
  Map<String, String> fragmentParameters();

  /**
   * @param key the key of the meta parameter
   * @return the value of the meta parameter of the specified key
   */
  MetaParam getMetaParameter(String key);

  /**
   * @return a copy of the request current meta parameters
   */
  Map<String, MetaParam> getMetaParameters();

  /**
   * @return all request parameters including query, path, headers, matrix, and fragment parameters
   */
  Map<String, List<String>> getRequestParameters();

  /**
   * @return the request http method
   */
  String getHttpMethod();

  /**
   * @return the accepted success codes
   */
  Integer[] getSuccessCodes();

  /**
   * @return the custom service root for this request
   */
  String getServiceRoot();

  /**
   * @return the writer class to be used for serializing the request body
   */
  RequestWriter<R> getRequestWriter();

  /**
   * @return the response reader associated with this request
   */
  ResponseReader<S> getResponseReader();

  /**
   * @return the path of the request
   */
  String getPath();

  /**
   * @return true if the request has been aborted, false otherwise
   */
  boolean isAborted();

  /**
   * @return true if the request does not have body, false otherwise
   */
  boolean isVoidRequest();

  /**
   * @return true if the request does not have response
   */
  boolean isVoidResponse();

  /**
   * @return the url of the request
   */
  String getUrl();

  /**
   * @return The url after matching with using the DynamicServiceRoot
   */
  String getMatchedUrl();

  /**
   * @return the timeout in milliseconds
   */
  int getTimeout();

  /**
   * @return the maximum retries of the request
   */
  int getMaxRetries();

  /**
   * @return the response type
   */
  String getResponseType();

  /**
   * Helper method that returns empty or the string of the value returned by the supplier
   *
   * @param supplier the supplier
   * @return the supplier value as a string, empty if null
   */
  String emptyOrStringValue(Supplier<?> supplier);

  /**
   * Helper method that formats the date based on a pattern
   *
   * @param supplier the date supplier
   * @param pattern the pattern
   * @return the formatted date
   */
  String formatDate(Supplier<Date> supplier, String pattern);

  /**
   * @return the request {@link NullQueryParamStrategy}
   */
  NullQueryParamStrategy getNullParamStrategy();

  /**
   * @return true if the request is a multipart form data, false otherwise
   */
  boolean isMultipartForm();
}

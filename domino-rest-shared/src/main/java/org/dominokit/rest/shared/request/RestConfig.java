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
import org.dominokit.rest.shared.request.service.annotations.DateFormat;

/** The global configurations for domino rest. */
public interface RestConfig {
  /**
   * Returns the default resource root for a specific path
   *
   * @param rootPath the path
   * @return the default resource root for the path
   */
  RestConfig setDefaultResourceRootPath(String rootPath);

  /** @return the router configured */
  RequestRouter<ServerRequest> getServerRouter();

  /** @return the default service root */
  String getDefaultServiceRoot();

  /** @return the default date format for JSON date fields */
  String getDefaultJsonDateFormat();

  /**
   * @return A list of the dynamic service roots configured
   * @see DynamicServiceRoot
   */
  List<DynamicServiceRoot> getServiceRoots();

  /**
   * Sets the default service root
   *
   * @param defaultServiceRoot the default service root
   * @return same instance to support builder pattern
   */
  RestConfig setDefaultServiceRoot(String defaultServiceRoot);

  /**
   * Sets t default JSON date format
   *
   * <p>For example: yyyy-MM-dd
   *
   * @param defaultJsonDateFormat the format
   * @return same instance to support builder pattern
   */
  RestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat);

  /**
   * Add new dynamic service root
   *
   * @param dynamicServiceRoot the service root to add
   * @return same instance to support builder pattern
   * @see DynamicServiceRoot
   */
  RestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot);

  /**
   * Adds new request interceptor
   *
   * @param interceptor the interceptor to add
   * @return same instance to support builder pattern
   * @see RequestInterceptor
   */
  RestConfig addRequestInterceptor(RequestInterceptor interceptor);

  /**
   * Removes request interceptor
   *
   * @param interceptor the interceptor to remove
   * @return same instance to support builder pattern
   * @see RequestInterceptor
   */
  RestConfig removeRequestInterceptor(RequestInterceptor interceptor);

  /**
   * @return all request interceptors configured
   * @see RequestInterceptor
   */
  List<RequestInterceptor> getRequestInterceptors();

  /**
   * Adds new response interceptor
   *
   * @param responseInterceptor the interceptor to add
   * @return same instance to support builder pattern
   * @see ResponseInterceptor
   */
  RestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor);

  /**
   * Removes response interceptor
   *
   * @param responseInterceptor the interceptor to remove
   * @return same instance to support builder pattern
   * @see ResponseInterceptor
   */
  RestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor);

  /**
   * @return all response interceptors configured
   * @see ResponseInterceptor
   */
  List<ResponseInterceptor> getResponseInterceptors();

  /** @return the default resource root configured */
  String getDefaultResourceRootPath();

  /**
   * Configures a default fail handler that will be called for each failed request
   *
   * @param fail the fail handler
   * @return same instance to support builder pattern
   * @see Fail
   */
  RestConfig setDefaultFailHandler(Fail fail);

  /**
   * @return the default fail handler configured
   * @see Fail
   */
  Fail getDefaultFailHandler();

  /**
   * @return the async runner configured
   * @see AsyncRunner
   */
  AsyncRunner asyncRunner();

  /**
   * Sets the date parameter formatter, this formatter will be called to format any parameter
   * annotated with {@link DateFormat}
   *
   * @param formatter the date parameter formatter
   * @return same instance to support builder pattern
   * @see DateParamFormatter
   * @see DateFormat
   */
  RestConfig setDateParamFormatter(DateParamFormatter formatter);

  /** @return the date parameter formatter configured */
  DateParamFormatter getDateParamFormatter();

  /**
   * Default strategy is {@link NullQueryParamStrategy#EMPTY}
   *
   * @return the {@link NullQueryParamStrategy}
   */
  default NullQueryParamStrategy getNullQueryParamStrategy() {
    return NullQueryParamStrategy.EMPTY;
  }

  /**
   * Sets the global strategy to handle query parameters with nul value
   *
   * @param strategy {@link NullQueryParamStrategy}
   * @return same instance
   */
  RestConfig setNullQueryParamStrategy(NullQueryParamStrategy strategy);

  UrlTokenRegexMatcher getUrlTokenRegexMatcher();

  Map<String, String> getGlobalPathParameters();

  Map<String, String> getGlobalHeaderParameters();

  Map<String, List<String>> getGlobalQueryParameters();

  RestConfig setGlobalPathParameter(String name, String value);

  RestConfig setGlobalPathParameters(Map<String, String> pathParameters);

  RestConfig setGlobalHeaderParameter(String name, String value);

  RestConfig setGlobalHeaderParameters(Map<String, String> headerParameters);

  RestConfig setGlobalQueryParameter(String name, String value);

  RestConfig addGlobalQueryParameter(String name, String value);

  RestConfig setGlobalQueryParameters(Map<String, List<String>> parameters);

  RestConfig addGlobalQueryParameters(Map<String, List<String>> parameters);

  /** Formatter to format the date parameter based on a patter */
  @FunctionalInterface
  interface DateParamFormatter {
    String format(Date date, String pattern);
  }
}

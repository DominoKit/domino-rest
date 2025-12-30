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

import java.util.List;
import java.util.Map;
import org.dominokit.rest.shared.regex.RegexEngine;

public class TestRestConfig implements RestConfig {

  private RegexValidationMode regexValidationMode = RegexValidationMode.FAIL;

  @Override
  public RestConfig setDefaultResourceRootPath(String rootPath) {
    return null;
  }

  @Override
  public RequestRouter<ServerRequest> getServerRouter() {
    return null;
  }

  @Override
  public String getDefaultServiceRoot() {
    return "";
  }

  @Override
  public String getDefaultJsonDateFormat() {
    return "";
  }

  @Override
  public List<DynamicServiceRoot> getServiceRoots() {
    return List.of();
  }

  @Override
  public RestConfig setDefaultServiceRoot(String defaultServiceRoot) {
    return null;
  }

  @Override
  public RestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat) {
    return null;
  }

  @Override
  public RestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot) {
    return null;
  }

  @Override
  public RestConfig addRequestInterceptor(RequestInterceptor interceptor) {
    return null;
  }

  @Override
  public RestConfig removeRequestInterceptor(RequestInterceptor interceptor) {
    return null;
  }

  @Override
  public List<RequestInterceptor> getRequestInterceptors() {
    return List.of();
  }

  @Override
  public RestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor) {
    return null;
  }

  @Override
  public RestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor) {
    return null;
  }

  @Override
  public List<ResponseInterceptor> getResponseInterceptors() {
    return List.of();
  }

  @Override
  public String getDefaultResourceRootPath() {
    return "";
  }

  @Override
  public RestConfig setDefaultFailHandler(Fail fail) {
    return null;
  }

  @Override
  public Fail getDefaultFailHandler() {
    return null;
  }

  @Override
  public AsyncRunner asyncRunner() {
    return null;
  }

  @Override
  public RestConfig setDateParamFormatter(DateParamFormatter formatter) {
    return null;
  }

  @Override
  public DateParamFormatter getDateParamFormatter() {
    return null;
  }

  @Override
  public RestConfig setNullQueryParamStrategy(NullQueryParamStrategy strategy) {
    return null;
  }

  @Override
  public RegexEngine getRegexEngine() {
    return new TestRegexEngine();
  }

  @Override
  public Map<String, String> getGlobalPathParameters() {
    return Map.of();
  }

  @Override
  public Map<String, String> getGlobalHeaderParameters() {
    return Map.of();
  }

  @Override
  public Map<String, List<String>> getGlobalQueryParameters() {
    return Map.of();
  }

  @Override
  public RestConfig setGlobalPathParameter(String name, String value) {
    return null;
  }

  @Override
  public RestConfig setGlobalPathParameters(Map<String, String> pathParameters) {
    return null;
  }

  @Override
  public RestConfig setGlobalHeaderParameter(String name, String value) {
    return null;
  }

  @Override
  public RestConfig setGlobalHeaderParameters(Map<String, String> headerParameters) {
    return null;
  }

  @Override
  public RestConfig setGlobalQueryParameter(String name, String value) {
    return null;
  }

  @Override
  public RestConfig addGlobalQueryParameter(String name, String value) {
    return null;
  }

  @Override
  public RestConfig setGlobalQueryParameters(Map<String, List<String>> parameters) {
    return null;
  }

  @Override
  public RestConfig addGlobalQueryParameters(Map<String, List<String>> parameters) {
    return null;
  }

  @Override
  public RestConfig setRegexValidationMode(RegexValidationMode regexValidationMode) {
    return this;
  }

  @Override
  public RegexValidationMode getRegexValidationMode() {
    return this.regexValidationMode;
  }
}

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
package org.dominokit.rest.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dominokit.jackson.annotation.JSONMapper;

@JSONMapper
public class TestResponse {

  private Map<String, String> headers = new HashMap<>();
  private Map<String, List<String>> queryParameters = new HashMap<>();
  private Map<String, String> pathParameters = new HashMap<>();

  private Map<String, List<String>> matrixParameters = new HashMap<>();
  private Map<String, String> fragmentParameters = new HashMap<>();

  public static TestResponse make() {
    return new TestResponse();
  };

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public Map<String, List<String>> getQueryParameters() {
    return queryParameters;
  }

  public void setQueryParameters(Map<String, List<String>> queryParameters) {
    this.queryParameters = queryParameters;
  }

  public Map<String, String> getPathParameters() {
    return pathParameters;
  }

  public void setPathParameters(Map<String, String> pathParameters) {
    this.pathParameters = pathParameters;
  }

  public Map<String, List<String>> getMatrixParameters() {
    return matrixParameters;
  }

  public void setMatrixParameters(Map<String, List<String>> matrixParameters) {
    this.matrixParameters = matrixParameters;
  }

  public Map<String, String> getFragmentParameters() {
    return fragmentParameters;
  }

  public void setFragmentParameters(Map<String, String> fragmentParameters) {
    this.fragmentParameters = fragmentParameters;
  }

  public TestResponse addHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  public TestResponse addQueryParameter(String name, String value) {
    this.queryParameters.put(name, Collections.singletonList(value));
    return this;
  }

  public TestResponse addQueryParameter(String name, Collection<String> value) {
    this.queryParameters.put(name, new ArrayList<>(value));
    return this;
  }

  public TestResponse addPathParameter(String name, String value) {
    this.pathParameters.put(name, value);
    return this;
  }

  public TestResponse addMatrixParameter(String name, String value) {
    this.matrixParameters.put(name, Collections.singletonList(value));
    return this;
  }

  public TestResponse addMatrixParameter(String name, Collection<String> value) {
    this.matrixParameters.put(name, new ArrayList<>(value));
    return this;
  }

  @Override
  public String toString() {
    return "TestResponse{"
        + "headers="
        + headers
        + ", queryParameters="
        + queryParameters
        + ", pathParameters="
        + pathParameters
        + ", matrixParameters="
        + matrixParameters
        + ", fragmentParameters="
        + fragmentParameters
        + '}';
  }
}

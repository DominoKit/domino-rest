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

/** An enum for configure the handling of null query parameters value */
public enum NullQueryParamStrategy {
  /**
   * set the parameter value as <b>null</b>
   *
   * <p>Example
   *
   * <pre>param1=null</pre>
   */
  NULL((request, name) -> request.setQueryParameter(name, "null")),
  /**
   * set the parameter value as empty
   *
   * <p>Example
   *
   * <pre>param1=</pre>
   */
  EMPTY((request, name) -> request.setQueryParameter(name, "")),
  /** Omit the parameter from the request query string */
  OMIT((request, name) -> {});

  private final ParamValueSetter paramValueSetter;

  NullQueryParamStrategy(ParamValueSetter paramValueSetter) {
    this.paramValueSetter = paramValueSetter;
  }

  /**
   * Sets a null query param value in the provided request
   *
   * @param request {@link ServerRequest}
   * @param name String name of the query parameter
   */
  public void setNullValue(ServerRequest<?, ?> request, String name) {
    paramValueSetter.setParameter(request, name);
  }

  private interface ParamValueSetter {
    void setParameter(ServerRequest<?, ?> request, String name);
  }
}

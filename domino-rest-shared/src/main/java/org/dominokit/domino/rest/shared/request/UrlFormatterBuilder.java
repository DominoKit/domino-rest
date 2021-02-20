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
package org.dominokit.domino.rest.shared.request;

import java.util.Map;

/**
 * A builder for {@link UrlFormatter}
 *
 * @see UrlFormatter
 */
public class UrlFormatterBuilder {
  private Map<String, String> queryParameters;
  private Map<String, String> pathParameters;

  /**
   * Sets the query parameters
   *
   * @param queryParameters the parameters
   * @return same instance
   */
  public UrlFormatterBuilder setQueryParameters(Map<String, String> queryParameters) {
    this.queryParameters = queryParameters;
    return this;
  }

  /**
   * Sets the path parameters
   *
   * @param pathParameters the parameters
   * @return same instance
   */
  public UrlFormatterBuilder setPathParameters(Map<String, String> pathParameters) {
    this.pathParameters = pathParameters;
    return this;
  }

  /** @return new url formatter */
  public UrlFormatter build() {
    return new UrlFormatter(queryParameters, pathParameters);
  }
}

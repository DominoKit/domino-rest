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

import java.util.Map;

/** A throwable represents the failed response */
public class FailedResponse extends Throwable {

  /** The HTTP status code. */
  private final int statusCode;

  /** The response status text. */
  private final String responseText;

  /** The response body as string. */
  private final String bodyAsString;

  /** The response headers. */
  private final Map<String, String> headers;

  /**
   * Creates a new instance.
   *
   * @param statusCode the status code
   * @param responseText the response text
   * @param bodyAsString the response body as string
   * @param headers the response headers
   */
  public FailedResponse(
      int statusCode, String responseText, String bodyAsString, Map<String, String> headers) {
    this.statusCode = statusCode;
    this.responseText = responseText;
    this.bodyAsString = bodyAsString;
    this.headers = headers;
  }

  /**
   * @return the status code of the response
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * @return the response text
   */
  public String getResponseText() {
    return responseText;
  }

  /**
   * @return the body as a string
   */
  public String getBodyAsString() {
    return bodyAsString;
  }

  /**
   * @return the response headers
   */
  public Map<String, String> getHeaders() {
    return headers;
  }
}

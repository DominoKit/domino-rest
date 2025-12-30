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

import static java.util.Objects.isNull;

import java.util.List;
import java.util.Map;
import org.dominokit.rest.shared.Response;

/**
 * A context represents the failed response
 *
 * @see ResponseBean
 */
public class FailedResponseBean implements ResponseBean {

  private static final long serialVersionUID = 7146258885910449957L;

  /** The HTTP status code. */
  private int statusCode;

  /** The response status text. */
  private String statusText;

  /** The response body as string. */
  private String body;

  /** The response headers. */
  private Map<String, List<String>> headers;

  /** The throwable cause of the failure. */
  private Throwable throwable;

  /** Default constructor. */
  public FailedResponseBean() {}

  /**
   * Creates a failed response bean with a throwable.
   *
   * @param throwable the throwable cause of the failure
   */
  public FailedResponseBean(Throwable throwable) {
    this.throwable = throwable;
  }

  /**
   * Creates a failed response bean from a request and a response.
   *
   * @param request the server request
   * @param response the response
   * @param <R> the request type
   * @param <S> the response type
   */
  public <R, S> FailedResponseBean(ServerRequest<R, S> request, Response response) {
    this.statusCode = response.getStatusCode();
    this.statusText = response.getStatusText();
    this.body = getResponseTextBody(request, response);
    this.headers = response.getHeaders();
  }

  private <R, S> String getResponseTextBody(ServerRequest<R, S> request, Response response) {
    if (isNull(request.getResponseType())
        || request.getResponseType().isEmpty()
        || "text".equalsIgnoreCase(request.getResponseType())) {
      return response.getBodyAsString();
    }
    return "";
  }

  /**
   * @return the status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * @return the status text
   */
  public String getStatusText() {
    return statusText;
  }

  /**
   * @return the response body as string
   */
  public String getBody() {
    return body;
  }

  /**
   * @return the response headers
   */
  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  /**
   * @return the throwable cause of the failure if any
   */
  public Throwable getThrowable() {
    return throwable;
  }

  /**
   * @param statusCode the status code to set
   */
  protected void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  /**
   * @param statusText the status text to set
   */
  protected void setStatusText(String statusText) {
    this.statusText = statusText;
  }

  /**
   * @param body the body to set
   */
  protected void setBody(String body) {
    this.body = body;
  }

  /**
   * @param headers the headers to set
   */
  protected void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  /**
   * @param throwable the throwable to set
   */
  protected void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }
}

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

import java.util.Map;
import org.dominokit.rest.shared.Response;

/**
 * A context represents the failed response
 *
 * @see ResponseBean
 */
public class FailedResponseBean implements ResponseBean {

  private static final long serialVersionUID = 7146258885910449957L;

  private int statusCode;
  private String statusText;
  private String body;
  private Map<String, String> headers;
  private Throwable throwable;

  public FailedResponseBean() {}

  public FailedResponseBean(Throwable throwable) {
    this.throwable = throwable;
  }

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

  public int getStatusCode() {
    return statusCode;
  }

  public String getStatusText() {
    return statusText;
  }

  public String getBody() {
    return body;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  protected void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  protected void setStatusText(String statusText) {
    this.statusText = statusText;
  }

  protected void setBody(String body) {
    this.body = body;
  }

  protected void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  protected void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }
}

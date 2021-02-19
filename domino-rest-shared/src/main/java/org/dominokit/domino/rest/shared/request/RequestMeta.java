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

/** The meta of the request */
public class RequestMeta {

  private Class<?> serviceClass;
  private String methodName;
  private Class<?> requestClass;
  private Class<?> responseClass;
  private String[] consume;
  private String[] produce;

  public RequestMeta(
      Class<?> serviceClass, String methodName, Class<?> requestClass, Class<?> responseClass) {
    this.serviceClass = serviceClass;
    this.methodName = methodName;
    this.requestClass = requestClass;
    this.responseClass = responseClass;
  }

  public Class<?> getServiceClass() {
    return serviceClass;
  }

  public String getMethodName() {
    return methodName;
  }

  public Class<?> getRequestClass() {
    return requestClass;
  }

  public Class<?> getResponseClass() {
    return responseClass;
  }

  void setConsume(String[] consume) {
    this.consume = consume;
  }

  void setProduce(String[] produce) {
    this.produce = produce;
  }

  public String[] getConsume() {
    return consume;
  }

  public String[] getProduce() {
    return produce;
  }

  @Override
  public String toString() {
    return "RequestMeta{"
        + "serviceClass="
        + serviceClass
        + ", methodName='"
        + methodName
        + '\''
        + ", requestClass="
        + requestClass
        + ", responseClass="
        + responseClass
        + '}';
  }
}

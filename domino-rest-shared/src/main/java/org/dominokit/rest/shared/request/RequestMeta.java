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

/** The meta of the request */
public class RequestMeta {

  private final Class<?> serviceClass;
  private final String methodName;
  private final Class<?> requestClass;
  private final Class<?> responseClass;
  private String[] consume;
  private String[] produce;
  private RequestParametersProvider parametersProvider = new RequestParametersProvider() {};

  /**
   * Creates a new instance.
   *
   * @param serviceClass the service class
   * @param methodName the method name
   * @param requestClass the request class
   * @param responseClass the response class
   */
  public RequestMeta(
      Class<?> serviceClass, String methodName, Class<?> requestClass, Class<?> responseClass) {
    this.serviceClass = serviceClass;
    this.methodName = methodName;
    this.requestClass = requestClass;
    this.responseClass = responseClass;
  }

  /**
   * @return the service class
   */
  public Class<?> getServiceClass() {
    return serviceClass;
  }

  /**
   * @return the method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return the request class
   */
  public Class<?> getRequestClass() {
    return requestClass;
  }

  /**
   * @return the response class
   */
  public Class<?> getResponseClass() {
    return responseClass;
  }

  /**
   * @param consume the consume media types
   */
  void setConsume(String[] consume) {
    this.consume = consume;
  }

  /**
   * @param produce the produce media types
   */
  void setProduce(String[] produce) {
    this.produce = produce;
  }

  /**
   * @param parametersProvider the parameters provider
   */
  void setParametersProvider(RequestParametersProvider parametersProvider) {
    this.parametersProvider = parametersProvider;
  }

  /**
   * @return the consume media types
   */
  public String[] getConsume() {
    return consume;
  }

  /**
   * @return the produce media types
   */
  public String[] getProduce() {
    return produce;
  }

  /**
   * @return the parameters provider
   */
  public RequestParametersProvider getParametersProvider() {
    return parametersProvider;
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

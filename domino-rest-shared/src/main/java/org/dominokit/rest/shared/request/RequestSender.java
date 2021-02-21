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

import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.dominokit.domino.api.shared.extension.ContextAggregator;
import org.dominokit.rest.shared.Response;
import org.dominokit.rest.shared.RestfulRequest;

/**
 * The default request rest sender.
 *
 * <p>This will send the request in the following steps:
 *
 * <ul>
 *   <li>Normalizes the url to replace the expressions
 *   <li>Calling the interceptors
 *   <li>Use the writer to write the body if needed
 *   <li>Send the request
 * </ul>
 *
 * @param <R> the request type
 * @param <S> the response type
 * @see ServerRequest
 * @see ServerRequestCallBack
 * @see RequestInterceptor
 */
public class RequestSender<R, S> implements RequestRestSender<R, S> {

  private static final Logger LOGGER = Logger.getLogger(RequestSender.class.getName());

  private final List<String> SEND_BODY_METHODS = Arrays.asList("POST", "PUT", "PATCH");

  /** {@inheritDoc} */
  @Override
  public void send(ServerRequest<R, S> request, ServerRequestCallBack callBack) {
    request.normalizeUrl();
    List<RequestInterceptor> interceptors =
        DominoRestContext.make().getConfig().getRequestInterceptors();

    if (nonNull(interceptors) && !interceptors.isEmpty()) {
      List<InterceptorRequestWait> interceptorsWaitList =
          interceptors.stream().map(InterceptorRequestWait::new).collect(Collectors.toList());
      ContextAggregator.waitFor(interceptorsWaitList)
          .onReady(() -> onAfterInterception(request, callBack));
      interceptorsWaitList.forEach(
          interceptorWait ->
              interceptorWait.getInterceptor().interceptRequest(request, interceptorWait));
    } else {
      onAfterInterception(request, callBack);
    }
  }

  private void onAfterInterception(ServerRequest<R, S> request, ServerRequestCallBack callBack) {
    final int[] retriesCounter = new int[] {0};
    RestfulRequest restfulRequest =
        RestfulRequest.request(request.getUrl(), request.getHttpMethod().toUpperCase());
    request.setHttpRequest(restfulRequest);
    if (!request.isAborted()) {
      restfulRequest
          .putHeaders(request.headers())
          .putParameters(request.queryParameters())
          .onSuccess(response -> handleResponse(request, callBack, response))
          .onError(
              throwable ->
                  handleError(request, callBack, retriesCounter, restfulRequest, throwable));

      if (nonNull(request.getResponseType())) {
        restfulRequest.setResponseType(request.getResponseType());
      }

      setTimeout(request, restfulRequest);
      setWithCredentials(request, restfulRequest);
      doSendRequest(request, restfulRequest);
    }
  }

  private void handleError(
      ServerRequest<R, S> request,
      ServerRequestCallBack callBack,
      int[] retriesCounter,
      RestfulRequest restfulRequest,
      Throwable throwable) {
    if (throwable instanceof RequestTimeoutException
        && retriesCounter[0] < request.getMaxRetries()) {
      retriesCounter[0]++;
      LOGGER.info("Retrying request : " + retriesCounter[0]);
      doSendRequest(request, restfulRequest);
    } else {
      FailedResponseBean failedResponse = new FailedResponseBean(throwable);
      LOGGER.log(Level.SEVERE, "Failed to execute request : ", failedResponse.getThrowable());
      callFailedResponseHandlers(request, failedResponse);
      callBack.onFailure(failedResponse);
    }
  }

  private void handleResponse(
      ServerRequest<R, S> request, ServerRequestCallBack callBack, Response response) {
    if (Arrays.stream(request.getSuccessCodes())
        .anyMatch(code -> code.equals(response.getStatusCode()))) {
      callSuccessGlobalHandlers(request, response);
      callBack.onSuccess(request.getResponseReader().read(response));
    } else {
      FailedResponseBean failedResponse = new FailedResponseBean(request, response);
      callFailedResponseHandlers(request, failedResponse);
      callBack.onFailure(failedResponse);
    }
  }

  private void callSuccessGlobalHandlers(ServerRequest<R, S> request, Response response) {
    DominoRestContext.make()
        .getConfig()
        .getResponseInterceptors()
        .forEach(responseInterceptor -> responseInterceptor.interceptOnSuccess(request, response));
  }

  private void callFailedResponseHandlers(
      ServerRequest request, FailedResponseBean failedResponse) {
    DominoRestContext.make()
        .getConfig()
        .getResponseInterceptors()
        .forEach(
            responseInterceptor -> responseInterceptor.interceptOnFailed(request, failedResponse));
  }

  private void setTimeout(ServerRequest<R, S> request, RestfulRequest restfulRequest) {
    if (request.getTimeout() > 0) {
      restfulRequest.timeout(request.getTimeout());
    }
  }

  private void setWithCredentials(ServerRequest<R, S> request, RestfulRequest restfulRequest) {
    if (request.getWithCredentialsRequest().isPresent()) {
      restfulRequest.setWithCredentials(
          request.getWithCredentialsRequest().get().isWithCredentials());
    }
  }

  private void doSendRequest(ServerRequest<R, S> request, RestfulRequest restfulRequest) {
    if (SEND_BODY_METHODS.contains(request.getHttpMethod().toUpperCase())
        && !request.isVoidRequest()) {
      restfulRequest.send(request.getRequestWriter().write(request.requestBean()));
    } else {
      restfulRequest.send();
    }
  }
}

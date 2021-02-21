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

import static java.util.Objects.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import org.dominokit.rest.shared.RestfulRequest;

/**
 * This class represents all the requests sent using domino rest, it provides the state workflow for
 * the request.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public class ServerRequest<R, S> extends BaseRequest
    implements Response<S>, HasComplete, HasHeadersAndParameters<R, S> {

  private static final Logger LOGGER = Logger.getLogger(ServerRequest.class.getName());

  private static final String CONTENT_TYPE = "Content-type";
  private static final String ACCEPT = "Accept";

  private final SenderSupplier<R, S> senderSupplier =
      new SenderSupplier<>(() -> new RequestSender<R, S>() {});

  private final Map<String, String> headers = new HashMap<>();
  private final Map<String, String> queryParameters = new HashMap<>();
  private final Map<String, String> pathParameters = new HashMap<>();
  private final Map<String, String> metaParameters = new HashMap<>();

  private RequestMeta requestMeta;
  private R requestBean;
  private RestfulRequest httpRequest;

  private String url;
  private String httpMethod = HttpMethod.GET;
  private String path = "";
  private String serviceRoot = "";
  private Integer[] successCodes = new Integer[] {200, 201, 202, 203, 204};
  private boolean voidResponse = false;

  private int timeout = -1;
  private int maxRetries = -1;
  private Optional<WithCredentialsRequest> withCredentialsRequest = Optional.empty();

  private RequestWriter<R> requestWriter = request -> null;
  private ResponseReader<S> responseReader = request -> null;

  private Success<S> success = response -> {};

  private final RequestState<ServerSuccessRequestStateContext> executedOnServer =
      context -> {
        success.onSuccess((S) context.responseBean);
        state = completed;
        completeHandler.onCompleted();
      };

  private final RequestState<ServerSuccessRequestStateContext> aborted =
      context -> {
        LOGGER.info("Request have already been aborted.!");
        completeHandler.onCompleted();
      };

  private final RequestState<ServerResponseReceivedStateContext> sent =
      context -> {
        if (state.equals(aborted)) {
          LOGGER.info("Request aborted, not response will be processed.");
        } else {
          if (context.nextContext instanceof ServerSuccessRequestStateContext) {
            state = executedOnServer;
            ServerRequest.this.applyState(context.nextContext);
          } else if (context.nextContext instanceof ServerFailedRequestStateContext) {
            state = failedOnServer;
            ServerRequest.this.applyState(context.nextContext);
          } else {
            throw new InvalidRequestState(
                "Request cannot be processed until a responseBean is received from the server");
          }
        }
      };
  private String responseType;

  protected ServerRequest() {}

  protected ServerRequest(RequestMeta requestMeta, R requestBean) {
    this.requestMeta = requestMeta;
    this.requestBean = requestBean;
  }

  /** prepare the request and execute it. */
  @Override
  public final void send() {
    execute();
  }

  /**
   * Sets the request body as a request bean
   *
   * @param requestBean the body
   * @return same instance to support builder pattern
   * @see RequestBean
   */
  public ServerRequest<R, S> setBean(R requestBean) {
    this.requestBean = requestBean;
    return this;
  }

  /**
   * Use this method to intercept the request before it is sent to the server, this is good for
   * setting headers or adding extra parameters.
   *
   * @param interceptor {@link Consumer} of {@link HasHeadersAndParameters}
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> intercept(Consumer<HasHeadersAndParameters<R, S>> interceptor) {
    interceptor.accept(this);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void startRouting() {
    state = sent;
    requestContext.getConfig().getServerRouter().routeRequest(this);
  }

  void setHttpRequest(RestfulRequest httpRequest) {
    this.httpRequest = httpRequest;
  }

  /** {@inheritDoc} */
  @Override
  public void abort() {
    if (state.equals(ready)) {
      state = aborted;
    } else if (state.equals(sent)) {
      if (nonNull(httpRequest)) {
        httpRequest.abort();
      }
      state = aborted;
      LOGGER.info("Request have been aborted : " + this.getClass().getCanonicalName());
    } else if (state.equals(completed)) {
      LOGGER.info("Could not abort request, request have already been completed.!");
    }
  }

  /**
   * Sets with credentials for this request
   *
   * @param withCredentials boolean to indicate if this request supports {@code withCredentials}
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> setWithCredentials(boolean withCredentials) {
    this.withCredentialsRequest = Optional.of(new WithCredentialsRequest(withCredentials));
    return this;
  }

  /** @return Optional if the with credentials is supported by this request */
  public Optional<WithCredentialsRequest> getWithCredentialsRequest() {
    return withCredentialsRequest;
  }

  /** {@inheritDoc} */
  @Override
  public RequestMeta getMeta() {
    return requestMeta;
  }

  /**
   * @return the rest sender associated with this request
   * @see RequestRestSender
   */
  public RequestRestSender<R, S> getSender() {
    return senderSupplier.get();
  }

  /**
   * @return the request bean of the request
   * @see RequestBean
   */
  public R requestBean() {
    return this.requestBean;
  }

  /** {@inheritDoc} */
  @Override
  public ServerRequest<R, S> setHeader(String name, String value) {
    headers.put(name, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public ServerRequest<R, S> setHeaders(Map<String, String> headers) {
    if (nonNull(headers) && !headers.isEmpty()) {
      this.headers.putAll(headers);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public ServerRequest<R, S> setQueryParameter(String name, String value) {
    queryParameters.put(name, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public ServerRequest<R, S> setQueryParameters(Map<String, String> queryParameters) {
    if (nonNull(queryParameters) && !queryParameters.isEmpty()) {
      this.queryParameters.putAll(queryParameters);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasHeadersAndParameters<R, S> setPathParameters(Map<String, String> pathParameters) {
    if (nonNull(pathParameters) && !pathParameters.isEmpty()) {
      this.pathParameters.putAll(pathParameters);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasHeadersAndParameters<R, S> setPathParameter(String name, String value) {
    pathParameters.put(name, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasHeadersAndParameters<R, S> setHeaderParameters(Map<String, String> headerParameters) {
    headers.putAll(headerParameters);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasHeadersAndParameters<R, S> setHeaderParameter(String name, String value) {
    headers.put(name, value);
    return this;
  }

  /** @return new map containing all headers defined in the request */
  public Map<String, String> headers() {
    return new HashMap<>(headers);
  }

  /** @return new map containing all headers defined in the request */
  public Map<String, String> queryParameters() {
    return new HashMap<>(queryParameters);
  }

  /** @return new map containing all headers defined in the request */
  public Map<String, String> pathParameters() {
    return new HashMap<>(pathParameters);
  }

  /**
   * apply the service root and resource root configuration and replace the variable parameters in
   * the request url.
   */
  public void normalizeUrl() {
    if (isNull(this.url)) {
      String root =
          (isNull(this.serviceRoot) || this.serviceRoot.isEmpty())
              ? ServiceRootMatcher.matchedServiceRoot(path)
              : (this.serviceRoot + path);
      UrlFormatter urlFormatter =
          new UrlFormatterBuilder()
              .setPathParameters(pathParameters)
              .setQueryParameters(queryParameters)
              .build();
      this.setUrl(urlFormatter.formatUrl(root));
    }
  }

  /**
   * These parameters will not be part of the generated code or will be by default part of the final
   * rest request We can use those parameters inside of a request interceptor to apply some
   * conditional logic
   *
   * @param key parameter key
   * @param value parameter value
   * @return the current request instance
   */
  public ServerRequest<R, S> setMetaParameter(String key, String value) {
    this.metaParameters.put(key, value);
    return this;
  }

  /**
   * @param key the key of the meta parameter
   * @return the value of the meta parameter of the specified key
   */
  public String getMetaParameter(String key) {
    return metaParameters.get(key);
  }

  /** @return a copy of the request current meta parameters */
  public Map<String, String> getMetaParameters() {
    return new HashMap<>(metaParameters);
  }

  /**
   * override the request url, when the url is set it is used as is, and no configuration or
   * parameter replacement is used.
   *
   * @param url the url of the request
   * @return same request instance.
   */
  public ServerRequest<R, S> setUrl(String url) {
    this.url = url;
    return this;
  }

  /**
   * add an on before send handler
   *
   * @param handler the handler to be called
   * @return same request instance.
   */
  public ServerRequest<R, S> onBeforeSend(BeforeSendHandler handler) {
    handler.onBeforeSend();
    return this;
  }

  /** @return new map of all added call arguments. */
  public Map<String, String> getRequestParameters() {
    Map<String, String> result = new HashMap<>();
    result.putAll(queryParameters);
    result.putAll(pathParameters);
    result.putAll(headers);
    return result;
  }

  /**
   * define the on success handler
   *
   * @param success the handler
   * @return same instance to support builder pattern
   */
  @Override
  public HasComplete onSuccess(Success<S> success) {
    this.success = success;
    return this;
  }

  /**
   * define the on complete handler
   *
   * @param completeHandler the handler
   * @return same instance to support builder pattern
   */
  @Override
  public CanFailOrSend onComplete(CompleteHandler completeHandler) {
    if (nonNull(completeHandler)) {
      this.completeHandler = completeHandler;
    }
    return this;
  }

  /**
   * sets the Content-type header
   *
   * @param contentType the content type array
   * @return same request instance.
   */
  public ServerRequest<R, S> setContentType(String[] contentType) {
    requestMeta.setConsume(contentType);
    setHeader(CONTENT_TYPE, String.join(", ", contentType));
    return this;
  }

  /**
   * sets the Accept header
   *
   * @param accept the accept array
   * @return same request instance.
   */
  public ServerRequest<R, S> setAccept(String[] accept) {
    requestMeta.setProduce(accept);
    setHeader(ACCEPT, String.join(", ", accept));
    return this;
  }

  /**
   * sets the http method
   *
   * @param httpMethod the method
   * @return same request instance.
   */
  public ServerRequest<R, S> setHttpMethod(String httpMethod) {
    requireNonNull(httpMethod);
    this.httpMethod = httpMethod.toUpperCase();
    return this;
  }

  /** @return the request http method */
  public String getHttpMethod() {
    return httpMethod;
  }

  /** @return the accepted succees codes */
  public Integer[] getSuccessCodes() {
    return successCodes;
  }

  /**
   * sets an array of integers to be considered as success response status code as success.
   *
   * @param successCodes {@link Integer[]}
   * @return same instance
   */
  public ServerRequest<R, S> setSuccessCodes(Integer[] successCodes) {
    this.successCodes = successCodes;
    return this;
  }

  /** @return the custom service root for this request. */
  public String getServiceRoot() {
    return serviceRoot;
  }

  /**
   * sets the service root for this request
   *
   * @param serviceRoot String
   * @return same request instance.
   */
  public ServerRequest<R, S> setServiceRoot(String serviceRoot) {
    this.serviceRoot = serviceRoot;
    return this;
  }

  /** @return the writer class to be used for serializing the request body */
  public RequestWriter<R> getRequestWriter() {
    if (nonNull(requestWriter)) {
      return requestWriter;
    } else {
      Optional<? extends RequestWriter<?>> reader = CustomMappersRegistry.INSTANCE.findWriter(this);
      if (reader.isPresent()) {
        return (RequestWriter<R>) reader.get();
      } else {
        throw new NoRequestWriterFoundForRequest(this);
      }
    }
  }

  /**
   * sets the writer to be used to serialize the request body
   *
   * @param requestWriter {@link RequestWriter}
   * @return same instance
   */
  public ServerRequest<R, S> setRequestWriter(RequestWriter<R> requestWriter) {
    this.requestWriter = requestWriter;
    return this;
  }

  /** @return the response reader associated with this request */
  public ResponseReader<S> getResponseReader() {
    if (nonNull(responseReader)) {
      return responseReader;
    } else {
      Optional<? extends ResponseReader<?>> reader =
          CustomMappersRegistry.INSTANCE.findReader(this);
      if (reader.isPresent()) {
        return (ResponseReader<S>) reader.get();
      } else {
        throw new NoResponseReaderFoundForRequest(this);
      }
    }
  }

  /**
   * Sets the response reader for this request
   *
   * @param responseReader the reader
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> setResponseReader(ResponseReader<S> responseReader) {
    this.responseReader = responseReader;
    return this;
  }

  /** @return the path of the request */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path of the request
   *
   * @param path the path
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> setPath(String path) {
    this.path = path;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public CanCompleteOrSend onFailed(Fail fail) {
    this.fail = fail;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAborted() {
    return state.equals(aborted);
  }

  /** @return true if the request does not have body, false otherwise */
  public boolean isVoidRequest() {
    return requestBean instanceof VoidRequest;
  }

  /** @return true if the request does not have response */
  public boolean isVoidResponse() {
    return voidResponse;
  }

  protected void markAsVoidResponse() {
    this.voidResponse = true;
  }

  /** @return the url of the request */
  public String getUrl() {
    return this.url;
  }

  /** @return the timeout in milliseconds */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Sets the timeout of the request in milliseconds
   *
   * @param timeout integer represents the timeout in milliseconds
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /** @return the maximum retries of the request */
  public int getMaxRetries() {
    return maxRetries;
  }

  /**
   * Sets the maximum retries for this request
   *
   * @param maxRetries number of maximum retries
   */
  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  /**
   * Sets the response type
   *
   * @param responseType the type
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> setResponseType(String responseType) {
    this.responseType = responseType;
    return this;
  }

  /** @return the response type */
  public String getResponseType() {
    return responseType;
  }

  /**
   * Helper method called from the generated requests that return empty or the string of the value
   * returned by the supplier
   *
   * @param supplier the supplier
   * @return the supplier value as a string, empty if null
   */
  public String emptyOrStringValue(Supplier<?> supplier) {
    if (isNull(supplier) || isNull(supplier.get())) {
      return "";
    }
    return String.valueOf(supplier.get());
  }

  /**
   * Helper method called from the generated requests that format the date based on a pattern
   *
   * @param supplier the date supplier
   * @param pattern the pattern
   * @return the formatted date
   */
  public String formatDate(Supplier<Date> supplier, String pattern) {
    if (isNull(supplier) || isNull(supplier.get())) {
      return "";
    }
    return emptyOrStringValue(
        () -> requestContext.getConfig().getDateParamFormatter().format(supplier.get(), pattern));
  }

  @FunctionalInterface
  public interface BeforeSendHandler {
    void onBeforeSend();
  }

  public static class WithCredentialsRequest {
    private final boolean withCredentials;

    public WithCredentialsRequest(boolean withCredentials) {
      this.withCredentials = withCredentials;
    }

    public boolean isWithCredentials() {
      return withCredentials;
    }
  }
}

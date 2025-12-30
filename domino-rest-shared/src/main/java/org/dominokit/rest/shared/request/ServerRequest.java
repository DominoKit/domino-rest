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

import jakarta.ws.rs.HttpMethod;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.dominokit.rest.shared.RestfulRequest;

/**
 * This class represents all the requests sent using domino rest, it provides the state workflow for
 * the request.
 *
 * @param <R> the request type
 * @param <S> the response type
 */
public class ServerRequest<R, S> extends BaseRequest
    implements Response<S>, HasComplete, HasParameters<R, S>, IServerRequest<R, S> {

  private static final Logger LOGGER = Logger.getLogger(ServerRequest.class.getName());

  private static final String CONTENT_TYPE = "Content-type";
  private static final String ACCEPT = "Accept";

  private final SenderSupplier<R, S> senderSupplier =
      new SenderSupplier<>(() -> new RequestSender<R, S>() {});

  private final Map<String, String> headers = new HashMap<>();
  private final Map<String, List<String>> queryParameters = new HashMap<>();
  private final Map<String, String> pathParameters = new HashMap<>();

  // NEW: matrix parameters support
  private final Map<String, List<String>> matrixParameters = new HashMap<>();
  // NEW: fragment parameters (used for {name} or {name:regex} inside the fragment part)
  private final Map<String, String> fragmentParameters = new HashMap<>();
  private final Map<String, MetaParam> metaParameters = new HashMap<>();

  private RequestMeta requestMeta;
  private R requestBean;
  private RestfulRequest httpRequest;

  private String url;
  private String matchedUrl;
  private String httpMethod;
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
        onCompleted();
      };

  private final RequestState<ServerSuccessRequestStateContext> aborted =
      context -> {
        LOGGER.info("Request have already been aborted.!");
        onCompleted();
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
  private NullQueryParamStrategy nullQueryParamStrategy;
  private boolean multipartForm = false;
  private RequestParametersProvider parametersProvider = new DefaultParametersProvider<R, S>(this);

  /** Default constructor. */
  protected ServerRequest() {
    this.httpMethod = HttpMethod.GET;
  }

  /**
   * Creates a new instance.
   *
   * @param requestMeta the request meta
   * @param requestBean the request bean
   */
  protected ServerRequest(RequestMeta requestMeta, R requestBean) {
    this.requestMeta = requestMeta;
    this.requestBean = requestBean;
    this.httpMethod = HttpMethod.GET;
    this.requestMeta.setParametersProvider(parametersProvider);
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
   * @param interceptor {@link Consumer} of {@link HasParameters}
   * @return same instance to support builder pattern
   */
  public ServerRequest<R, S> intercept(Consumer<HasParameters<R, S>> interceptor) {
    interceptor.accept(this);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public void startRouting() {
    state = sent;
    requestContext.getConfig().getServerRouter().routeRequest(this);
  }

  /**
   * Sets the HTTP request.
   *
   * @param httpRequest the HTTP request
   */
  public void setHttpRequest(RestfulRequest httpRequest) {
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
    queryParameters.put(name, new ArrayList<>());
    addQueryParameter(name, value);
    return this;
  }

  @Override
  public HasParameters<R, S> addQueryParameter(String name, String value) {
    if (queryParameters.containsKey(name)) {
      queryParameters.get(name).add(value);
    } else {
      setQueryParameter(name, value);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public ServerRequest<R, S> setQueryParameters(Map<String, List<String>> parameters) {
    parameters
        .keySet()
        .forEach(name -> parameters.get(name).forEach(value -> addQueryParameter(name, value)));
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasParameters<R, S> addQueryParameters(Map<String, List<String>> parameters) {
    parameters.forEach(
        (key, values) -> {
          values.forEach(value -> addQueryParameter(key, value));
        });
    return this;
  }

  // -------------------- Matrix parameters (NEW) --------------------

  @Override
  public HasParameters<R, S> setMatrixParameter(String name, String value) {
    matrixParameters.put(name, new ArrayList<>());
    addMatrixParameter(name, value);
    return this;
  }

  @Override
  public HasParameters<R, S> setMatrixParameter(String name, List<String> values) {
    matrixParameters.put(name, new ArrayList<>());
    values.forEach(v -> addMatrixParameter(name, v));
    return this;
  }

  @Override
  public HasParameters<R, S> setMatrixParameters(Map<String, List<String>> matrixParameters) {
    if (nonNull(matrixParameters) && !matrixParameters.isEmpty()) {
      matrixParameters.forEach(this::setMatrixParameter);
    }
    return this;
  }

  @Override
  public HasParameters<R, S> addMatrixParameter(String name, String value) {
    if (matrixParameters.containsKey(name)) {
      matrixParameters.get(name).add(value);
    } else {
      setMatrixParameter(name, value);
    }
    return this;
  }

  @Override
  public HasParameters<R, S> addMatrixParameter(String name, List<String> values) {
    if (matrixParameters.containsKey(name)) {
      matrixParameters.get(name).addAll(values);
    } else {
      setMatrixParameter(name, values);
    }
    return this;
  }

  @Override
  public HasParameters<R, S> addMatrixParameters(Map<String, List<String>> matrixParameters) {
    if (nonNull(matrixParameters) && !matrixParameters.isEmpty()) {
      matrixParameters.forEach(this::addMatrixParameter);
    }
    return this;
  }

  /** Exposes a defensive copy of matrix params (optional helper). */
  public Map<String, List<String>> matrixParameters() {
    Map<String, List<String>> copy = new HashMap<>();
    matrixParameters.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
    return copy;
  }

  // ---------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public HasParameters<R, S> setPathParameters(Map<String, String> pathParameters) {
    if (nonNull(pathParameters) && !pathParameters.isEmpty()) {
      this.pathParameters.putAll(pathParameters);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasParameters<R, S> setPathParameter(String name, String value) {
    pathParameters.put(name, value);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasParameters<R, S> setHeaderParameters(Map<String, String> headerParameters) {
    headers.putAll(headerParameters);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public HasParameters<R, S> setHeaderParameter(String name, String value) {
    headers.put(name, value);
    return this;
  }

  public ServerRequest<R, S> setFragmentParameter(String name, String value) {
    if (name != null) fragmentParameters.put(name, value);
    return this;
  }

  public ServerRequest<R, S> setFragmentParameters(Map<String, String> params) {
    if (params != null && !params.isEmpty()) fragmentParameters.putAll(params);
    return this;
  }

  public Map<String, String> fragmentParameters() {
    return new HashMap<>(fragmentParameters);
  }

  /** @return new map containing all headers defined in the request */
  public Map<String, String> headers() {
    return new HashMap<>(headers);
  }

  /** @return new map containing all query parameters defined in the request */
  public Map<String, List<String>> queryParameters() {
    return new HashMap<>(queryParameters);
  }

  /** @return new map containing all path parameters defined in the request */
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
              ? ServiceRootMatcher.matchedServiceRoot(new ImmutableServerRequest<>(this))
              : insureBackSlash(this.serviceRoot, path);

      this.matchedUrl = root;

      // PATH params: globals + request-scoped
      Map<String, String> combinedPathParams = new HashMap<>();
      combinedPathParams.putAll(DominoRestContext.make().getConfig().getGlobalPathParameters());
      combinedPathParams.putAll(pathParameters);

      // Build UrlFormatter with per-component maps:
      // - PATH: combinedPathParams
      // - MATRIX: single-value view (for template placeholders like ;{k}={v})
      // - QUERY: single-value view (for placeholders like ?{k}={v})
      // - FRAGMENT: explicit fragmentParameters map (new)
      UrlFormatter urlFormatter =
          new UrlFormatter(
              combinedPathParams,
              toSingleValueMap(matrixParameters),
              toSingleValueMap(queryParameters),
              fragmentParameters);

      UrlSplitUtil.Split result =
          new UrlSplitUtil(DominoRestContext.make().getConfig().getRegexEngine()).split(root);
      String tokenString = result.rightSide;
      String serviceRoot = result.leftSide;

      String formatted = urlFormatter.formatUrl(tokenString);

      ServicePath sp = new ServicePath(formatted);
      // Preserve multiplicity of matrix params by appending all values to the LAST segment
      if (!matrixParameters.isEmpty()) {
        List<String> segments = sp.paths();
        if (!segments.isEmpty()) {
          int lastIdx = segments.size() - 1;
          matrixParameters.forEach(
              (name, values) -> values.forEach(v -> sp.appendMatrixParameter(lastIdx, name, v)));
        }
      }

      if (!queryParameters.isEmpty()) {
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
          sp.setQueryParameter(entry.getKey(), entry.getValue());
        }
      }

      formatted = sp.value();

      this.setUrl(insureBackSlash(serviceRoot, formatted));
    }
  }

  private String insureBackSlash(String lh, String rh) {
    return (lh.endsWith("/") || rh.startsWith("/")) ? (lh + rh) : (lh + "/" + rh);
  }

  // Helper: collapse matrix map (List<String>) to a single-value map for UrlFormatter
  // used only for placeholder replacement in matrix *names* inside templates, e.g. ;{k}={v}
  // For multiple values, explicit append via ServicePath happens above.
  private static Map<String, String> toSingleValueMap(Map<String, List<String>> multi) {
    Map<String, String> single = new HashMap<>();
    multi.forEach(
        (k, v) -> {
          if (v != null && !v.isEmpty()) single.put(k, v.get(0));
        });
    return single;
  }

  /**
   * These parameters will not be part of the generated code or will be by default part of the final
   * rest request We can use those parameters inside of a request interceptor to apply some
   * conditional logic
   *
   * @param metaParam parameter value
   * @return the current request instance
   */
  public ServerRequest<R, S> setMetaParameter(MetaParam metaParam) {
    this.metaParameters.put(metaParam.getName(), metaParam);
    return this;
  }

  /**
   * @param key the key of the meta parameter
   * @return the value of the meta parameter of the specified key
   */
  public MetaParam getMetaParameter(String key) {
    return metaParameters.get(key);
  }

  /** @return a copy of the request current meta parameters */
  public Map<String, MetaParam> getMetaParameters() {
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

  public Map<String, List<String>> getRequestParameters() {
    Map<String, List<String>> result = new HashMap<>();
    result.putAll(queryParameters);
    pathParameters.forEach((k, v) -> result.put(k, Collections.singletonList(v)));
    headers.forEach((k, v) -> result.put(k, Collections.singletonList(v)));
    matrixParameters.forEach((k, values) -> result.put(k, new ArrayList<>(values)));
    // NEW: fragment params as single-valued entries
    fragmentParameters.forEach((k, v) -> result.put(k, Collections.singletonList(v)));
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
   * define the on complete handler
   *
   * @param completeHandler the handler
   * @return same instance to support builder pattern
   */
  @Override
  public CanFailOrSend onAfterComplete(CompleteHandler completeHandler) {
    if (nonNull(completeHandler)) {
      this.afterCompleteHandler = completeHandler;
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

  @Override
  public String getMatchedUrl() {
    return this.matchedUrl;
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

  /**
   * @return the request {@link NullQueryParamStrategy} and if not set fallback to the Global
   *     strategy defined in
   */
  public NullQueryParamStrategy getNullParamStrategy() {
    if (isNull(nullQueryParamStrategy)) {
      return DominoRestContext.make().getConfig().getNullQueryParamStrategy();
    }
    return nullQueryParamStrategy;
  }

  /**
   * Overrides the {@link NullQueryParamStrategy} defined in {@link
   * RestConfig#getDateParamFormatter()} for this request
   *
   * @param strategy {@link NullQueryParamStrategy}
   * @return same instance
   */
  public ServerRequest<R, S> setNullQueryParamStrategy(NullQueryParamStrategy strategy) {
    if (nonNull(strategy)) {
      this.nullQueryParamStrategy = strategy;
    }
    return this;
  }

  /** @return true if the request is a multipart form data, false otherwise */
  public boolean isMultipartForm() {
    return multipartForm;
  }

  /** @param multipartForm true to mark the request as a multipart form data */
  public void setMultipartForm(boolean multipartForm) {
    this.multipartForm = multipartForm;
  }

  /** Handler for before send events. */
  @FunctionalInterface
  public interface BeforeSendHandler {
    /** Called before sending the request. */
    void onBeforeSend();
  }

  /** Represents a request with credentials. */
  public static class WithCredentialsRequest {
    private final boolean withCredentials;

    /**
     * Creates a new instance.
     *
     * @param withCredentials true if with credentials is enabled, false otherwise
     */
    public WithCredentialsRequest(boolean withCredentials) {
      this.withCredentials = withCredentials;
    }

    /** @return true if with credentials is enabled, false otherwise */
    public boolean isWithCredentials() {
      return withCredentials;
    }
  }

  private static class DefaultParametersProvider<R, S> implements RequestParametersProvider {

    private final ServerRequest<R, S> serverRequest;

    public DefaultParametersProvider(ServerRequest<R, S> serverRequest) {
      this.serverRequest = serverRequest;
    }

    @Override
    public Map<String, String> getHeaders() {
      return new HashMap<>(serverRequest.headers());
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
      return new HashMap<>(serverRequest.queryParameters());
    }

    @Override
    public Map<String, String> getPathParameters() {
      return new HashMap<>(serverRequest.pathParameters());
    }

    @Override
    public Map<String, List<String>> getMatrixParameters() {
      return new HashMap<>(serverRequest.matrixParameters());
    }

    @Override
    public Map<String, String> getFragmentParameters() {
      return new HashMap<>(serverRequest.fragmentParameters());
    }

    @Override
    public Map<String, MetaParam> getMetaParameters() {
      return new HashMap<>(serverRequest.getMetaParameters());
    }
  }
}

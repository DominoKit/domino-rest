package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.rest.shared.RestfulRequest;

import javax.ws.rs.HttpMethod;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.Objects.*;

public class ServerRequest<R, S>
        extends BaseRequest implements Response<S>, CanFailOrSend, HasHeadersAndParameters<R, S> {

    private static final Logger LOGGER = Logger.getLogger(ServerRequest.class.getName());

    private static final String CONTENT_TYPE = "Content-type";
    private static final String ACCEPT = "Accept";

    //should remain anonymous till a bug is fixed in j2cl
    private SenderSupplier<R, S> senderSupplier = new SenderSupplier<>(() -> new RequestSender<R, S>() {
    });

    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> pathParameters = new HashMap<>();

    private RequestMeta requestMeta;
    private R requestBean;
    private RestfulRequest httpRequest;

    private String url;
    private String httpMethod = HttpMethod.GET;
    private String path = "";
    private String serviceRoot = "";
    private Integer[] successCodes = new Integer[]{200, 201, 202, 203, 204};
    private boolean voidResponse = false;

    private int timeout = -1;
    private int maxRetries = -1;

    private RequestWriter<R> requestWriter = request -> null;
    private ResponseReader<S> responseReader = request -> null;

    private Success<S> success = response -> {
    };

    private final RequestState<ServerSuccessRequestStateContext> executedOnServer = context -> {
        success.onSuccess((S) context.responseBean);
        state = completed;
    };

    private final RequestState<ServerSuccessRequestStateContext> aborted = context -> {
        LOGGER.info("Request have already been aborted.!");
    };

    private final RequestState<ServerResponseReceivedStateContext> sent = context -> {
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

    protected ServerRequest() {
    }

    protected ServerRequest(RequestMeta requestMeta, R requestBean) {
        this.requestMeta = requestMeta;
        this.requestBean = requestBean;
    }

    /**
     * prepare the request and execute it.
     */
    @Override
    public final void send() {
        execute();
    }

    public ServerRequest<R, S> setBean(R requestBean) {
        this.requestBean = requestBean;
        return this;
    }

    /**
     * Use this method to intercept the request before it is sent to the server, this is good for setting headers or adding extra parameters.
     *
     * @param interceptor
     * @return the same request instance
     */
    public ServerRequest<R, S> intercept(Consumer<HasHeadersAndParameters<R, S>> interceptor) {
        interceptor.accept(this);
        return this;
    }

    @Override
    public void startRouting() {
        state = sent;
        requestContext.getConfig().getServerRouter().routeRequest(this);
    }

    void setHttpRequest(RestfulRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

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

    @Override
    public void setWithCredentials(boolean withCredentials) {
        httpRequest.setWithCredentials(withCredentials);
    }

    @Override
    public RequestMeta getMeta() {
        return requestMeta;
    }

    public RequestRestSender getSender() {
        return senderSupplier.get();
    }

    public R requestBean() {
        return this.requestBean;
    }

    /**
     * set a request a header, this will add a new header or override existing one
     *
     * @param name
     * @param value
     * @return the same request instance
     */
    public ServerRequest<R, S> setHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * sets request headers from a map
     *
     * @param headers
     * @return the same request instance
     */
    public ServerRequest<R, S> setHeaders(Map<String, String> headers) {
        if (nonNull(headers) && !headers.isEmpty()) {
            this.headers.putAll(headers);
        }
        return this;
    }

    /**
     * sets a query parameter or override an existing one
     *
     * @param name
     * @param value
     * @return the same request instance
     */
    public ServerRequest<R, S> setQueryParameter(String name, String value) {
        queryParameters.put(name, value);
        return this;
    }

    /**
     * set request query parameters from a map
     *
     * @param queryParameters
     * @return the same request instance
     */
    public ServerRequest<R, S> setQueryParameters(Map<String, String> queryParameters) {
        if (nonNull(queryParameters) && !queryParameters.isEmpty()) {
            this.queryParameters.putAll(queryParameters);
        }
        return this;
    }

    /**
     * use {@link #setQueryParameter(String, String)}
     */
    @Deprecated
    public ServerRequest<R, S> setParameter(String name, String value) {
        queryParameters.put(name, value);
        return this;
    }

    /**
     * use {@link #setQueryParameters(Map)}
     */
    @Deprecated
    public ServerRequest<R, S> setParameters(Map<String, String> queryParameters) {
        if (nonNull(queryParameters) && !queryParameters.isEmpty()) {
            this.queryParameters.putAll(queryParameters);
        }
        return this;
    }

    @Override
    public HasHeadersAndParameters<R, S> setPathParameters(Map<String, String> pathParameters) {
        if (nonNull(pathParameters) && !pathParameters.isEmpty()) {
            this.pathParameters.putAll(pathParameters);
        }
        return this;
    }

    @Override
    public HasHeadersAndParameters<R, S> setPathParameter(String name, String value) {
        pathParameters.put(name, value);
        return this;
    }

    @Override
    public HasHeadersAndParameters<R, S> setHeaderParameters(Map<String, String> headerParameters) {
        headers.putAll(headerParameters);
        return this;
    }

    @Override
    public HasHeadersAndParameters<R, S> setHeaderParameter(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * @returnnew map containing all headers defined in the request
     */
    public Map<String, String> headers() {
        return new HashMap<>(headers);
    }

    /**
     * @return new map containing all headers defined in the request
     */
    public Map<String, String> queryParameters() {
        return new HashMap<>(queryParameters);
    }

    /**
     * @return new map containing all headers defined in the request
     */
    public Map<String, String> pathParameters() {
        return new HashMap<>(pathParameters);
    }

    /**
     * apply the service root and resource root configuration and replace the variable parameters in the request url.
     */
    public void normalizeUrl() {
        if (isNull(this.url)) {
            String root = (isNull(this.serviceRoot) || this.serviceRoot.isEmpty()) ? ServiceRootMatcher.matchedServiceRoot(path) : (this.serviceRoot + path);
            UrlFormatter<R> urlFormatter = new UrlFormatterBuilder<R>()
                    .setPathParameters(pathParameters)
                    .setQueryParameters(queryParameters)
                    .setRequestBean(requestBean)
                    .build();
            this.setUrl(urlFormatter.formatUrl(root));
        }
    }

    /**
     * override the request url, when the url is set it is used as is, and no configuration or parameter replacement is used.
     *
     * @param url
     * @return same request instance.
     */
    public ServerRequest<R, S> setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * add an on before send handler
     *
     * @param handler
     * @return same request instance.
     */
    public ServerRequest<R, S> onBeforeSend(BeforeSendHandler handler) {
        handler.onBeforeSend();
        return this;
    }


    /**
     * @return new map of all added call arguments.
     */
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
     * @param success
     * @return
     */
    @Override
    public CanFailOrSend onSuccess(Success<S> success) {
        this.success = success;
        return this;
    }

    /**
     * sets the Content-type header
     *
     * @param contentType
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
     * @param accept
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
     * @param httpMethod
     * @return same request instance.
     */
    public ServerRequest<R, S> setHttpMethod(String httpMethod) {
        requireNonNull(httpMethod);
        this.httpMethod = httpMethod.toUpperCase();
        return this;
    }

    /**
     * @return the request http method
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * @return the accepted succees codes
     */
    public Integer[] getSuccessCodes() {
        return successCodes;
    }

    /**
     * sets an array of integers to be considered as success response status code as success.
     *
     * @param successCodes
     * @return
     */
    public ServerRequest<R, S> setSuccessCodes(Integer[] successCodes) {
        this.successCodes = successCodes;
        return this;
    }

    /**
     * @return the custom service root for this request.
     */
    public String getServiceRoot() {
        return serviceRoot;
    }

    /**
     * sets the service root for this request
     *
     * @param serviceRoot
     * @return same request instance.
     */
    public ServerRequest<R, S> setServiceRoot(String serviceRoot) {
        this.serviceRoot = serviceRoot;
        return this;
    }

    /**
     * @return the writer class to be used for serializing the request body
     */
    public RequestWriter<R> getRequestWriter() {
        if (nonNull(requestWriter)) {
            return requestWriter;
        } else {
            Optional<? extends RequestWriter<?>> reader = CustomMappersRegistry.INSTANCE.findWriter(this);
            if (reader.isPresent()) {
                return (RequestWriter<R>) reader.get();
            } else {
                throw new NoResponseReaderFoundForRequest(this);
            }
        }
    }

    /**
     * sets the writer to be used to serialize the request body
     *
     * @param requestWriter
     */
    public ServerRequest<R, S> setRequestWriter(RequestWriter<R> requestWriter) {
        this.requestWriter = requestWriter;
        return this;
    }

    public ResponseReader<S> getResponseReader() {
        if (nonNull(responseReader)) {
            return responseReader;
        } else {
            Optional<? extends ResponseReader<?>> reader = CustomMappersRegistry.INSTANCE.findReader(this);
            if (reader.isPresent()) {
                return (ResponseReader<S>) reader.get();
            } else {
                throw new NoResponseReaderFoundForRequest(this);
            }
        }
    }

    public ServerRequest<R, S> setResponseReader(ResponseReader<S> responseReader) {
        this.responseReader = responseReader;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ServerRequest<R, S> setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public CanSend onFailed(Fail fail) {
        this.fail = fail;
        return this;
    }

    @Override
    public boolean isAborted() {
        return state.equals(aborted);
    }

    public boolean isVoidRequest() {
        return requestBean instanceof VoidRequest;
    }

    public boolean isVoidResponse() {
        return voidResponse;
    }

    protected void markAsVoidResponse() {
        this.voidResponse = true;
    }

    public String getUrl() {
        return this.url;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public ServerRequest<R, S> setResponseType(String responseType) {
        this.responseType = responseType;
        return this;
    }

    public String getResponseType() {
        return responseType;
    }

    public String emptyOrStringValue(Supplier<?> supplier) {
        if (isNull(supplier) || isNull(supplier.get())) {
            return "";
        }
        return String.valueOf(supplier.get());
    }

    public String formatDate(Supplier<Date> supplier, String pattern) {
        if (isNull(supplier) || isNull(supplier.get())) {
            return "";
        }
        return emptyOrStringValue(() -> requestContext
                .getConfig()
                .getDateParamFormatter()
                .format(supplier.get(), pattern));
    }

    @FunctionalInterface
    public interface BeforeSendHandler {
        void onBeforeSend();
    }
}

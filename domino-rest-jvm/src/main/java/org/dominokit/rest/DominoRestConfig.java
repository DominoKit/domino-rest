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
package org.dominokit.rest;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dominokit.jackson.JacksonContextProvider;
import org.dominokit.rest.jvm.DefaultServiceRoot;
import org.dominokit.rest.jvm.OnServerRequestEventFactory;
import org.dominokit.rest.shared.request.*;

/**
 * Default {@link RestConfig} implementation for server side
 *
 * @see RestConfig
 */
public class DominoRestConfig implements RestConfig {

  private static final Logger LOGGER = Logger.getLogger(DominoRestConfig.class.getName());

  private static String defaultServiceRoot;
  private static String defaultResourceRootPath = "service";
  private static String defaultJsonDateFormat = null;

  private static RequestRouter<ServerRequest> serverRouter =
      new ServerRouter(
          new DefaultRequestAsyncSender(new OnServerRequestEventFactory(), new RequestSender<>()));
  private static List<DynamicServiceRoot> dynamicServiceRoots = new ArrayList<>();
  private static List<RequestInterceptor> requestInterceptors = new ArrayList<>();
  private static final List<ResponseInterceptor> responseInterceptors = new ArrayList<>();
  private static Fail defaultFailHandler =
      failedResponse -> {
        if (nonNull(failedResponse.getThrowable())) {
          LOGGER.log(
              Level.FINE, "could not execute request on server: ", failedResponse.getThrowable());
        } else {
          LOGGER.log(
              Level.FINE,
              "could not execute request on server: status ["
                  + failedResponse.getStatusCode()
                  + "], body ["
                  + failedResponse.getBody()
                  + "]");
        }
      };

  private static DateParamFormatter dateParamFormatter =
      (date, pattern) -> new SimpleDateFormat(pattern).format(date);

  private static NullQueryParamStrategy nullQueryParamStrategy = NullQueryParamStrategy.EMPTY;

  /**
   * Gets and initialize the instance with the default configurations
   *
   * @return the instance
   */
  public static DominoRestConfig initDefaults() {
    RestfullRequestContext.setFactory(new JavaRestfulRequestFactory());
    DominoRestContext.init(DominoRestConfig.getInstance());
    return DominoRestConfig.getInstance();
  }

  /** @return new instance */
  public static DominoRestConfig getInstance() {
    return new DominoRestConfig();
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig setDefaultServiceRoot(String defaultServiceRoot) {
    this.defaultServiceRoot = defaultServiceRoot;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig setDefaultJsonDateFormat(String defaultJsonDateFormat) {
    JacksonContextProvider.get().defaultDeserializerParameters().setPattern(defaultJsonDateFormat);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig addDynamicServiceRoot(DynamicServiceRoot dynamicServiceRoot) {
    dynamicServiceRoots.add(dynamicServiceRoot);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig addRequestInterceptor(RequestInterceptor interceptor) {
    this.requestInterceptors.add(interceptor);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig removeRequestInterceptor(RequestInterceptor interceptor) {
    this.requestInterceptors.remove(interceptor);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public List<RequestInterceptor> getRequestInterceptors() {
    return requestInterceptors;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig addResponseInterceptor(ResponseInterceptor responseInterceptor) {
    this.getResponseInterceptors().add(responseInterceptor);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig removeResponseInterceptor(ResponseInterceptor responseInterceptor) {
    this.getResponseInterceptors().remove(responseInterceptor);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public List<ResponseInterceptor> getResponseInterceptors() {
    return responseInterceptors;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig setDefaultFailHandler(Fail fail) {
    if (nonNull(fail)) {
      this.defaultFailHandler = fail;
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public Fail getDefaultFailHandler() {
    return defaultFailHandler;
  }

  /** {@inheritDoc} */
  @Override
  public String getDefaultServiceRoot() {
    if (isNull(defaultServiceRoot)) {
      return DefaultServiceRoot.get() + defaultResourceRootPath + "/";
    }
    return defaultServiceRoot;
  }

  /** {@inheritDoc} */
  @Override
  public String getDefaultJsonDateFormat() {
    return JacksonContextProvider.get().defaultDeserializerParameters().getPattern();
  }

  /** {@inheritDoc} */
  @Override
  public List<DynamicServiceRoot> getServiceRoots() {
    return dynamicServiceRoots;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig setDefaultResourceRootPath(String rootPath) {
    if (nonNull(rootPath)) {
      this.defaultResourceRootPath = rootPath;
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public RequestRouter<ServerRequest> getServerRouter() {
    return serverRouter;
  }

  /** {@inheritDoc} */
  @Override
  public String getDefaultResourceRootPath() {
    if (nonNull(defaultResourceRootPath) && !defaultResourceRootPath.trim().isEmpty()) {
      return defaultResourceRootPath + "/";
    } else {
      return "";
    }
  }

  /** {@inheritDoc} */
  @Override
  public AsyncRunner asyncRunner() {
    return asyncTask -> {
      try {
        asyncTask.onSuccess();
      } catch (Throwable error) {
        asyncTask.onFailed(error);
      }
    };
  }

  /**
   * Sets the server router
   *
   * @param serverRouter the new router
   */
  public void setServerRouter(RequestRouter<ServerRequest> serverRouter) {
    DominoRestConfig.serverRouter = serverRouter;
  }

  /** {@inheritDoc} */
  @Override
  public RestConfig setDateParamFormatter(DateParamFormatter formatter) {
    DominoRestConfig.dateParamFormatter = formatter;
    return this;
  }

  /** {@inheritDoc} */
  @Override
  public DateParamFormatter getDateParamFormatter() {
    return DominoRestConfig.dateParamFormatter;
  }

  /** {@inheritDoc} */
  @Override
  public NullQueryParamStrategy getNullQueryParamStrategy() {
    return nullQueryParamStrategy;
  }

  /** {@inheritDoc} */
  @Override
  public DominoRestConfig setNullQueryParamStrategy(NullQueryParamStrategy nullQueryParamStrategy) {
    if (nonNull(nullQueryParamStrategy)) {
      DominoRestConfig.nullQueryParamStrategy = nullQueryParamStrategy;
    }
    return this;
  }
}

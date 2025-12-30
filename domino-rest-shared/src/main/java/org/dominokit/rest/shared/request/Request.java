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

/**
 * A representation of the request sent by domino rest.
 *
 * <p>The request exposes two methods to start routing the request i.e. send it and to abort it.
 *
 * <p>The request goes through states, each state represents if the request sent successfully,
 * failed, and if the response recieved.
 *
 * @see RequestStateContext
 */
public interface Request {

  /** Default implementation of {@link RequestStateContext}. */
  class DefaultRequestStateContext implements RequestStateContext {}

  /** Context for the state when a server response is received. */
  class ServerResponseReceivedStateContext implements RequestStateContext {
    /** The next state context. */
    protected final RequestStateContext nextContext;

    /**
     * Creates a new instance.
     *
     * @param nextContext the next state context
     */
    public ServerResponseReceivedStateContext(RequestStateContext nextContext) {
      this.nextContext = nextContext;
    }
  }

  /**
   * Context for the state when a server request is successful.
   *
   * @param <T> the type of the response bean
   */
  class ServerSuccessRequestStateContext<T> implements RequestStateContext {

    /** The response bean. */
    protected final T responseBean;

    /**
     * Creates a new instance.
     *
     * @param responseBean the response bean
     */
    public ServerSuccessRequestStateContext(T responseBean) {
      this.responseBean = responseBean;
    }
  }

  /** Context for the state when a server request fails. */
  class ServerFailedRequestStateContext implements RequestStateContext {

    /** The failed response bean. */
    protected final FailedResponseBean response;

    /**
     * Creates a new instance.
     *
     * @param response the failed response bean
     */
    public ServerFailedRequestStateContext(FailedResponseBean response) {
      this.response = response;
    }
  }

  /** Send the request */
  void startRouting();

  /**
   * Set the state of the request to either success, failed, response recieved
   *
   * @param context the context of the state
   */
  void applyState(RequestStateContext context);

  /**
   * @return the meta associated with the request
   */
  default RequestMeta getMeta() {
    return null;
  }

  /** Abort the request */
  default void abort() {}

  /**
   * @return true if the request was aborted, false otherwise
   */
  default boolean isAborted() {
    return false;
  }

  /** Exception thrown when the request is in an invalid state. */
  class InvalidRequestState extends RuntimeException {

    private static final long serialVersionUID = 1976356149064117774L;

    /**
     * Creates a new instance.
     *
     * @param message the exception message
     */
    public InvalidRequestState(String message) {
      super(message);
    }
  }
}

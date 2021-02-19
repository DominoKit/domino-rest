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

  class DefaultRequestStateContext implements RequestStateContext {}

  class ServerResponseReceivedStateContext implements RequestStateContext {
    protected final RequestStateContext nextContext;

    public ServerResponseReceivedStateContext(RequestStateContext nextContext) {
      this.nextContext = nextContext;
    }
  }

  class ServerSuccessRequestStateContext<T> implements RequestStateContext {

    protected final T responseBean;

    public ServerSuccessRequestStateContext(T responseBean) {
      this.responseBean = responseBean;
    }
  }

  class ServerFailedRequestStateContext implements RequestStateContext {

    protected final FailedResponseBean response;

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

  /** @return the meta associated with the request */
  default RequestMeta getMeta() {
    return null;
  }

  /** Abort the request */
  default void abort() {}

  /** @return true if the request was aborted, false otherwise */
  default boolean isAborted() {
    return false;
  }

  class InvalidRequestState extends RuntimeException {

    private static final long serialVersionUID = 1976356149064117774L;

    public InvalidRequestState(String message) {
      super(message);
    }
  }
}

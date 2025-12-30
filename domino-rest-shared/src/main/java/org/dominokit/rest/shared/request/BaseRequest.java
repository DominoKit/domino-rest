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
 * A base implementation that updates the state of the request based on the result
 *
 * @see RequestState
 */
public abstract class BaseRequest implements Request {

  /** Error message when a request is sent more than once. */
  public static final String REQUEST_HAVE_ALREADY_BEEN_SENT = "Request have already been sent";

  /** The current state of the request. */
  protected RequestState state;

  private boolean skipFailHandler = false;

  /** The context for the REST request. */
  protected final DominoRestContext requestContext = DominoRestContext.make();

  /** State representing the request is ready to be sent. */
  protected final RequestState<DefaultRequestStateContext> ready = context -> startRouting();

  /** State representing the request has been completed. */
  protected final RequestState<DefaultRequestStateContext> completed =
      context -> {
        throw new InvalidRequestState(
            "This request have already been completed!. ["
                + this.getClass().getCanonicalName()
                + "]");
      };

  /** Handler to be called when the request is completed. */
  protected CompleteHandler completeHandler = () -> {};

  /** Handler to be called after the request is completed. */
  protected CompleteHandler afterCompleteHandler = () -> {};

  /** Handler to be called when the request fails. */
  protected Fail fail = requestContext.getConfig().getDefaultFailHandler();

  /** State representing the request failed on the server. */
  protected final RequestState<ServerFailedRequestStateContext> failedOnServer =
      context -> {
        if (!skipFailHandler) {
          fail.onFail(context.response);
        }
        onCompleted();
      };

  /** Internal method called when the request is completed to trigger handlers and interceptors. */
  protected void onCompleted() {
    DominoRestContext.make()
        .getConfig()
        .getResponseInterceptors()
        .forEach(
            responseInterceptor ->
                responseInterceptor.onBeforeCompleteCallback((ServerRequest) this));
    completeHandler.onCompleted();
    DominoRestContext.make()
        .getConfig()
        .getResponseInterceptors()
        .forEach(
            responseInterceptor ->
                responseInterceptor.onAfterCompleteCallback((ServerRequest) this));
    afterCompleteHandler.onCompleted();
  }

  /** Default constructor, initializes the request to the ready state. */
  public BaseRequest() {
    this.state = ready;
  }

  /** Executes the request if it is in a valid state. */
  protected void execute() {
    if (!state.equals(ready) && !state.equals(failedOnServer))
      throw new InvalidRequestState(REQUEST_HAVE_ALREADY_BEEN_SENT);
    this.state.execute(new DefaultRequestStateContext());
  }

  /** {@inheritDoc} */
  @Override
  public void applyState(RequestStateContext context) {
    state.execute(context);
  }

  /** Skip the failure handler if the request has failed */
  public void skipFailHandler() {
    this.skipFailHandler = true;
  }
}

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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Factory for creating one instance of the request sender
 *
 * @param <R> the request type
 * @param <S> the response type
 * @see RequestRestSender
 */
public class SenderSupplier<R, S> implements Supplier<RequestRestSender<R, S>> {

  private RequestRestSender<R, S> sender;
  private final Supplier<RequestRestSender<R, S>> senderFactory;

  public SenderSupplier(Supplier<RequestRestSender<R, S>> senderFactory) {
    this.senderFactory = senderFactory;
  }

  @Override
  public RequestRestSender<R, S> get() {
    if (Objects.isNull(sender)) this.sender = senderFactory.get();
    return sender;
  }
}

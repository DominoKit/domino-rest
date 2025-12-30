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

import java.util.function.Supplier;

/**
 * A context that holds the meta matcher with its correspondent writer
 *
 * @see MetaMatcher
 * @see RequestWriter
 */
public class RequestWriterMatcher {

  private final MetaMatcher metaMatcher;
  private final Supplier<RequestWriter<?>> writer;

  /**
   * Creates a new instance.
   *
   * @param metaMatcher the {@link MetaMatcher}
   * @param writer the {@link RequestWriter} supplier
   */
  RequestWriterMatcher(MetaMatcher metaMatcher, Supplier<RequestWriter<?>> writer) {
    this.metaMatcher = metaMatcher;
    this.writer = writer;
  }

  /**
   * @return the {@link MetaMatcher}
   */
  public MetaMatcher getMetaMatcher() {
    return metaMatcher;
  }

  /**
   * @return the {@link RequestWriter}
   */
  public RequestWriter<?> getWriter() {
    return writer.get();
  }
}

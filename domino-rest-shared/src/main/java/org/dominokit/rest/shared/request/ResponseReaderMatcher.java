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
 * A context that holds the meta matcher with its correspondent reader
 *
 * @see MetaMatcher
 * @see ResponseReader
 */
public class ResponseReaderMatcher {

  private final MetaMatcher metaMatcher;
  private final Supplier<ResponseReader<?>> reader;

  /**
   * Creates a new instance.
   *
   * @param metaMatcher the {@link MetaMatcher}
   * @param reader the {@link ResponseReader} supplier
   */
  ResponseReaderMatcher(MetaMatcher metaMatcher, Supplier<ResponseReader<?>> reader) {
    this.metaMatcher = metaMatcher;
    this.reader = reader;
  }

  /**
   * @return the {@link MetaMatcher}
   */
  public MetaMatcher getMetaMatcher() {
    return metaMatcher;
  }

  /**
   * @return the {@link ResponseReader}
   */
  public ResponseReader<?> getReader() {
    return reader.get();
  }
}

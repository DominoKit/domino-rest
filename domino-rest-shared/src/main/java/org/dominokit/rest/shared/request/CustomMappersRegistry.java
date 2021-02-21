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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/** A registry to register custom readers and writers for the requests */
public class CustomMappersRegistry {

  public static final CustomMappersRegistry INSTANCE = new CustomMappersRegistry();

  private static final List<RequestWriterMatcher> writerMatchers = new ArrayList<>();
  private static final List<ResponseReaderMatcher> readerMatchers = new ArrayList<>();

  private CustomMappersRegistry() {}

  /**
   * Register reader based on a matcher
   *
   * @param matcher the matcher
   * @param readerSupplier the reader supplier
   * @return same instance to support builder pattern
   */
  public CustomMappersRegistry registerResponseReader(
      MetaMatcher matcher, Supplier<ResponseReader<?>> readerSupplier) {
    readerMatchers.add(new ResponseReaderMatcher(matcher, readerSupplier));
    return this;
  }

  /**
   * Register writer based on a matcher
   *
   * @param matcher the matcher
   * @param writerSupplier the writer supplier
   * @return same instance to support builder pattern
   */
  public CustomMappersRegistry registerRequestWriter(
      MetaMatcher matcher, Supplier<RequestWriter<?>> writerSupplier) {
    writerMatchers.add(new RequestWriterMatcher(matcher, writerSupplier));
    return this;
  }

  Optional<? extends ResponseReader<?>> findReader(Request request) {
    return readerMatchers.stream()
        .filter(readers -> readers.getMetaMatcher().match(request.getMeta()))
        .map(ResponseReaderMatcher::getReader)
        .findFirst();
  }

  Optional<? extends RequestWriter<?>> findWriter(Request request) {
    return writerMatchers.stream()
        .filter(writers -> writers.getMetaMatcher().match(request.getMeta()))
        .map(RequestWriterMatcher::getWriter)
        .findFirst();
  }
}

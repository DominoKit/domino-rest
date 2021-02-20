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

import java.util.function.Supplier;

/**
 * The class is used to add custom {@link ResponseReader} or {@link RequestWriter} for a specific
 * endpoint without the need to add annotation to it.
 *
 * <p>The mapper matches the request based on its meta and then assign a reader or a writer to it.
 * Domino rest will read these custom readers/writers to serialize/deserialize the request
 *
 * @see RequestWriter
 * @see ResponseReader
 * @see MetaMatcher
 */
public class CustomMapper {

  private MetaMatcher matcher;

  private CustomMapper(MetaMatcher matcher) {
    this.matcher = matcher;
  }

  /**
   * Matches the request based on its meta
   *
   * @param matcher the matcher
   * @return same instance to support builder pattern
   */
  public static CustomMapper matcher(MetaMatcher matcher) {
    return new CustomMapper(matcher);
  }

  /**
   * Assign a reader for the request
   *
   * @param reader the response reader
   * @return same instance to support builder pattern
   */
  public CustomMapper reader(Supplier<ResponseReader<?>> reader) {
    CustomMappersRegistry.INSTANCE.registerResponseReader(matcher, reader);
    return this;
  }

  /**
   * Assign a writer for the request
   *
   * @param writer the request writer
   * @return same instance to support builder pattern
   */
  public CustomMapper writer(Supplier<RequestWriter<?>> writer) {
    CustomMappersRegistry.INSTANCE.registerRequestWriter(matcher, writer);
    return this;
  }
}

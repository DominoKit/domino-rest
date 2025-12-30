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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Represents a metadata parameter for a request. */
public class MetaParam {
  private final String name;
  private final String value;
  private final Set<MetaParam> metaParams = new HashSet<>();

  /**
   * Creates a new MetaParam with the specified name and value.
   *
   * @param name the name of the meta parameter
   * @param value the value of the meta parameter
   * @return a new MetaParam instance
   */
  public static MetaParam of(String name, String value) {
    return new MetaParam(name, value);
  }

  /**
   * Constructs a new MetaParam with the specified name and value.
   *
   * @param name the name of the meta parameter
   * @param value the value of the meta parameter
   */
  public MetaParam(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /** @return the name of the meta parameter */
  public String getName() {
    return name;
  }

  /** @return the value of the meta parameter */
  public String getValue() {
    return value;
  }

  /**
   * Adds a nested meta parameter.
   *
   * @param metaParam the meta parameter to add
   * @return this instance for chaining
   */
  public MetaParam addMetaParam(MetaParam metaParam) {
    this.metaParams.add(metaParam);
    return this;
  }

  /**
   * Adds a collection of nested meta parameters.
   *
   * @param metaParams the collection of meta parameters to add
   * @return this instance for chaining
   */
  public MetaParam addMetaParams(Collection<MetaParam> metaParams) {
    this.metaParams.addAll(metaParams);
    return this;
  }

  /** @return a set of nested meta parameters */
  public Set<MetaParam> getMetaParams() {
    return new HashSet<>(metaParams);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MetaParam metaParam = (MetaParam) o;
    return Objects.equals(name, metaParam.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }
}

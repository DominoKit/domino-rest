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

public class MetaParam {
  private final String name;
  private final String value;
  private final Set<MetaParam> metaParams = new HashSet<>();

  public static MetaParam of(String name, String value) {
    return new MetaParam(name, value);
  }

  public MetaParam(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public MetaParam addMetaParam(MetaParam metaParam) {
    this.metaParams.add(metaParam);
    return this;
  }

  public MetaParam addMetaParams(Collection<MetaParam> metaParams) {
    this.metaParams.addAll(metaParams);
    return this;
  }

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

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
package javax.ws.rs.core;
public class EntityTag {

  private String value;
  private boolean weak;

  public EntityTag(final String value) {
    this(value, false);
  }

  public EntityTag(final String value, final boolean weak) {
    if (value == null) {
      throw new IllegalArgumentException("value==null");
    }
    this.value = value;
    this.weak = weak;
  }

  public static EntityTag valueOf(final String value) {
    return null;
  }

  public boolean isWeak() {
    return weak;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EntityTag)) {
      return super.equals(obj);
    }
    EntityTag other = (EntityTag) obj;
    if (value.equals(other.getValue()) && weak == other.isWeak()) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + (this.value != null ? this.value.hashCode() : 0);
    hash = 17 * hash + (this.weak ? 1 : 0);
    return hash;
  }

  @Override
  public String toString() {
    return null;
  }
}

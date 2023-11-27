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

import javax.ws.rs.GwtIncompatible;


@GwtIncompatible
public class GenericEntity<T> {

  private final Class<?> rawType;
  private final T entity;

  
  protected GenericEntity(final T entity) {
    if (entity == null) {
      throw new IllegalArgumentException("The entity must not be null");
    }
    this.entity = entity;
    this.rawType = entity.getClass();
  }


  
  public final Class<?> getRawType() {
    return rawType;
  }

  
  public final T getEntity() {
    return entity;
  }

  @Override
  public boolean equals(Object obj) {
    return false;
  }

  @Override
  public int hashCode() {
    return entity.hashCode();
  }

  @Override
  public String toString() {
    return "GenericEntity{" + entity.toString()+ "}";
  }
}

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

public class Cookie {

  public static final int DEFAULT_VERSION = 1;

  private final String name;
  private final String value;
  private final int version;
  private final String path;
  private final String domain;

  public Cookie(
      final String name,
      final String value,
      final String path,
      final String domain,
      final int version)
      throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("name==null");
    }
    this.name = name;
    this.value = value;
    this.version = version;
    this.domain = domain;
    this.path = path;
  }

  
  public Cookie(final String name, final String value, final String path, final String domain)
      throws IllegalArgumentException {
    this(name, value, path, domain, DEFAULT_VERSION);
  }

  public Cookie(final String name, final String value) throws IllegalArgumentException {
    this(name, value, null, null);
  }

  public static Cookie valueOf(final String value) {
    return null;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public int getVersion() {
    return version;
  }

  public String getDomain() {
    return domain;
  }

  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
    hash = 97 * hash + this.version;
    hash = 97 * hash + (this.path != null ? this.path.hashCode() : 0);
    hash = 97 * hash + (this.domain != null ? this.domain.hashCode() : 0);
    return hash;
  }

  
  @SuppressWarnings({"StringEquality", "RedundantIfStatement"})
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Cookie other = (Cookie) obj;
    if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
      return false;
    }
    if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
      return false;
    }
    if (this.version != other.version) {
      return false;
    }
    if (this.path != other.path && (this.path == null || !this.path.equals(other.path))) {
      return false;
    }
    if (this.domain != other.domain && (this.domain == null || !this.domain.equals(other.domain))) {
      return false;
    }
    return true;
  }
}

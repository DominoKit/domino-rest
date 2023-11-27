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

import java.util.Date;
import javax.ws.rs.GwtIncompatible;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class NewCookie extends Cookie {

  
  public static final int DEFAULT_MAX_AGE = -1;

  private static final HeaderDelegate<NewCookie> delegate =
      RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class);

  private final String comment;
  private final int maxAge;
  private final Date expiry;
  private final boolean secure;
  private final boolean httpOnly;

  
  public NewCookie(String name, String value) {
    this(name, value, null, null, DEFAULT_VERSION, null, DEFAULT_MAX_AGE, null, false, false);
  }

  
  public NewCookie(
      String name,
      String value,
      String path,
      String domain,
      String comment,
      int maxAge,
      boolean secure) {
    this(name, value, path, domain, DEFAULT_VERSION, comment, maxAge, null, secure, false);
  }

  
  public NewCookie(
      String name,
      String value,
      String path,
      String domain,
      String comment,
      int maxAge,
      boolean secure,
      boolean httpOnly) {
    this(name, value, path, domain, DEFAULT_VERSION, comment, maxAge, null, secure, httpOnly);
  }

  
  public NewCookie(
      String name,
      String value,
      String path,
      String domain,
      int version,
      String comment,
      int maxAge,
      boolean secure) {
    this(name, value, path, domain, version, comment, maxAge, null, secure, false);
  }

  
  public NewCookie(
      String name,
      String value,
      String path,
      String domain,
      int version,
      String comment,
      int maxAge,
      Date expiry,
      boolean secure,
      boolean httpOnly) {
    super(name, value, path, domain, version);
    this.comment = comment;
    this.maxAge = maxAge;
    this.expiry = expiry;
    this.secure = secure;
    this.httpOnly = httpOnly;
  }

  
  public NewCookie(Cookie cookie) {
    this(cookie, null, DEFAULT_MAX_AGE, null, false, false);
  }

  
  public NewCookie(Cookie cookie, String comment, int maxAge, boolean secure) {
    this(cookie, comment, maxAge, null, secure, false);
  }

  
  public NewCookie(
      Cookie cookie, String comment, int maxAge, Date expiry, boolean secure, boolean httpOnly) {
    super(
        cookie == null ? null : cookie.getName(),
        cookie == null ? null : cookie.getValue(),
        cookie == null ? null : cookie.getPath(),
        cookie == null ? null : cookie.getDomain(),
        cookie == null ? Cookie.DEFAULT_VERSION : cookie.getVersion());
    this.comment = comment;
    this.maxAge = maxAge;
    this.expiry = expiry;
    this.secure = secure;
    this.httpOnly = httpOnly;
  }

  
  public static NewCookie valueOf(String value) {
    return delegate.fromString(value);
  }

  
  public String getComment() {
    return comment;
  }

  
  public int getMaxAge() {
    return maxAge;
  }

  
  public Date getExpiry() {
    return expiry;
  }

  
  public boolean isSecure() {
    return secure;
  }

  
  public boolean isHttpOnly() {
    return httpOnly;
  }

  
  public Cookie toCookie() {
    return new Cookie(
        this.getName(), this.getValue(), this.getPath(), this.getDomain(), this.getVersion());
  }

  
  @Override
  public String toString() {
    return delegate.toString(this);
  }

  
  @Override
  public int hashCode() {
    int hash = super.hashCode();
    hash = 59 * hash + (this.comment != null ? this.comment.hashCode() : 0);
    hash = 59 * hash + this.maxAge;
    hash = 59 + hash + (this.expiry != null ? this.expiry.hashCode() : 0);
    hash = 59 * hash + (this.secure ? 1 : 0);
    hash = 59 * hash + (this.httpOnly ? 1 : 0);
    return hash;
  }

  
  @SuppressWarnings({"StringEquality", "RedundantIfStatement"})
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NewCookie other = (NewCookie) obj;
    if (this.getName() != other.getName()
        && (this.getName() == null || !this.getName().equals(other.getName()))) {
      return false;
    }
    if (this.getValue() != other.getValue()
        && (this.getValue() == null || !this.getValue().equals(other.getValue()))) {
      return false;
    }
    if (this.getVersion() != other.getVersion()) {
      return false;
    }
    if (this.getPath() != other.getPath()
        && (this.getPath() == null || !this.getPath().equals(other.getPath()))) {
      return false;
    }
    if (this.getDomain() != other.getDomain()
        && (this.getDomain() == null || !this.getDomain().equals(other.getDomain()))) {
      return false;
    }
    if (this.comment != other.comment
        && (this.comment == null || !this.comment.equals(other.comment))) {
      return false;
    }
    if (this.maxAge != other.maxAge) {
      return false;
    }

    if (this.expiry != other.expiry && (this.expiry == null || !this.expiry.equals(other.expiry))) {
      return false;
    }

    if (this.secure != other.secure) {
      return false;
    }
    if (this.httpOnly != other.httpOnly) {
      return false;
    }
    return true;
  }
}

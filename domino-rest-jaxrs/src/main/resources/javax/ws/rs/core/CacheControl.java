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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GwtIncompatible;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

@GwtIncompatible
public class CacheControl {

  private static final HeaderDelegate<CacheControl> HEADER_DELEGATE =
      RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);
  private List<String> privateFields;
  private List<String> noCacheFields;
  private Map<String, String> cacheExtension;

  private boolean privateFlag;
  private boolean noCache;
  private boolean noStore;
  private boolean noTransform;
  private boolean mustRevalidate;
  private boolean proxyRevalidate;
  private int maxAge = -1;
  private int sMaxAge = -1;

  public CacheControl() {
    privateFlag = false;
    noCache = false;
    noStore = false;
    noTransform = true;
    mustRevalidate = false;
    proxyRevalidate = false;
  }

  public static CacheControl valueOf(final String value) {
    return HEADER_DELEGATE.fromString(value);
  }

  public boolean isMustRevalidate() {
    return mustRevalidate;
  }

  public void setMustRevalidate(final boolean mustRevalidate) {
    this.mustRevalidate = mustRevalidate;
  }

  public boolean isProxyRevalidate() {
    return proxyRevalidate;
  }

  public void setProxyRevalidate(final boolean proxyRevalidate) {
    this.proxyRevalidate = proxyRevalidate;
  }
  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(final int maxAge) {
    this.maxAge = maxAge;
  }

  public int getSMaxAge() {
    return sMaxAge;
  }

  
  public void setSMaxAge(final int sMaxAge) {
    this.sMaxAge = sMaxAge;
  }

  
  public List<String> getNoCacheFields() {
    if (noCacheFields == null) {
      noCacheFields = new ArrayList<String>();
    }
    return noCacheFields;
  }

  
  public void setNoCache(final boolean noCache) {
    this.noCache = noCache;
  }

  
  public boolean isNoCache() {
    return noCache;
  }

  
  public boolean isPrivate() {
    return privateFlag;
  }

  
  public List<String> getPrivateFields() {
    if (privateFields == null) {
      privateFields = new ArrayList<String>();
    }
    return privateFields;
  }

  
  public void setPrivate(final boolean flag) {
    this.privateFlag = flag;
  }

  
  public boolean isNoTransform() {
    return noTransform;
  }

  
  public void setNoTransform(final boolean noTransform) {
    this.noTransform = noTransform;
  }

  
  public boolean isNoStore() {
    return noStore;
  }

  
  public void setNoStore(final boolean noStore) {
    this.noStore = noStore;
  }

  
  public Map<String, String> getCacheExtension() {
    if (cacheExtension == null) {
      cacheExtension = new HashMap<String, String>();
    }
    return cacheExtension;
  }

  
  @Override
  public String toString() {
    return HEADER_DELEGATE.toString(this);
  }

  
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + (this.privateFlag ? 1 : 0);
    hash = 41 * hash + (this.noCache ? 1 : 0);
    hash = 41 * hash + (this.noStore ? 1 : 0);
    hash = 41 * hash + (this.noTransform ? 1 : 0);
    hash = 41 * hash + (this.mustRevalidate ? 1 : 0);
    hash = 41 * hash + (this.proxyRevalidate ? 1 : 0);
    hash = 41 * hash + this.maxAge;
    hash = 41 * hash + this.sMaxAge;
    hash = 41 * hash + hashCodeOf(this.privateFields);
    hash = 41 * hash + hashCodeOf(this.noCacheFields);
    hash = 41 * hash + hashCodeOf(this.cacheExtension);
    return hash;
  }

  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CacheControl other = (CacheControl) obj;
    if (this.privateFlag != other.privateFlag) {
      return false;
    }
    if (this.noCache != other.noCache) {
      return false;
    }
    if (this.noStore != other.noStore) {
      return false;
    }
    if (this.noTransform != other.noTransform) {
      return false;
    }
    if (this.mustRevalidate != other.mustRevalidate) {
      return false;
    }
    if (this.proxyRevalidate != other.proxyRevalidate) {
      return false;
    }
    if (this.maxAge != other.maxAge) {
      return false;
    }
    if (this.sMaxAge != other.sMaxAge) {
      return false;
    }
    if (notEqual(this.privateFields, other.privateFields)) {
      return false;
    }
    if (notEqual(this.noCacheFields, other.noCacheFields)) {
      return false;
    }
    if (notEqual(this.cacheExtension, other.cacheExtension)) {
      return false;
    }
    return true;
  }

  
  private static boolean notEqual(Collection<?> first, Collection<?> second) {
    if (first == second) {
      return false;
    }
    if (first == null) {
      // if first is 'null', consider equal to empty
      return !second.isEmpty();
    }
    if (second == null) {
      // if second is 'null', consider equal to empty
      return !first.isEmpty();
    }

    return !first.equals(second);
  }

  
  private static boolean notEqual(Map<?, ?> first, Map<?, ?> second) {
    if (first == second) {
      return false;
    }
    if (first == null) {
      // if first is 'null', consider equal to empty
      return !second.isEmpty();
    }
    if (second == null) {
      // if second is 'null', consider equal to empty
      return !first.isEmpty();
    }

    return !first.equals(second);
  }

  
  private static int hashCodeOf(Collection<?> instance) {
    return (instance == null || instance.isEmpty()) ? 0 : instance.hashCode();
  }

  
  private static int hashCodeOf(Map<?, ?> instance) {
    return (instance == null || instance.isEmpty()) ? 0 : instance.hashCode();
  }
}

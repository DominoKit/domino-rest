/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package jakarta.ws.rs.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.ws.rs.GwtIncompatible;

import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

@GwtIncompatible
public class CacheControl {

    @Deprecated
    private static final HeaderDelegate<CacheControl> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class);
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

    @Deprecated
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

    public void setSMaxAge(final int smaxAge) {
        this.sMaxAge = smaxAge;
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
    @Deprecated
    public String toString() {
        return HEADER_DELEGATE.toString(this);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(this.privateFlag, this.noCache, this.noStore, this.noTransform, this.mustRevalidate,
                this.proxyRevalidate, this.maxAge, this.sMaxAge);
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

    private static boolean notEqual(final Collection<?> first, final Collection<?> second) {
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

    private static boolean notEqual(final Map<?, ?> first, final Map<?, ?> second) {
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

    private static int hashCodeOf(final Collection<?> instance) {
        return (instance == null || instance.isEmpty()) ? 0 : instance.hashCode();
    }

    private static int hashCodeOf(final Map<?, ?> instance) {
        return (instance == null || instance.isEmpty()) ? 0 : instance.hashCode();
    }
}

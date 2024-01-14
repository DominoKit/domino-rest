/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.GwtIncompatible;
import java.util.Date;
import java.util.Objects;

import jakarta.ws.rs.ext.RuntimeDelegate;
import jakarta.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class NewCookie extends Cookie {

    public static final int DEFAULT_MAX_AGE = -1;

    @Deprecated
    private static final HeaderDelegate<NewCookie> DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(NewCookie.class);

    private final String comment;
    private final int maxAge;
    private final Date expiry;
    private final boolean secure;
    private final boolean httpOnly;
    private final SameSite sameSite;

    @Deprecated
    public NewCookie(final String name, final String value) {
        this(name, value, null, null, DEFAULT_VERSION, null, DEFAULT_MAX_AGE, null, false, false, null);
    }

    @Deprecated
    public NewCookie(final String name,
            final String value,
            final String path,
            final String domain,
            final String comment,
            final int maxAge,
            final boolean secure) {
        this(name, value, path, domain, DEFAULT_VERSION, comment, maxAge, null, secure, false, null);
    }

    @Deprecated
    public NewCookie(final String name,
            final String value,
            final String path,
            final String domain,
            final String comment,
            final int maxAge,
            final boolean secure,
            final boolean httpOnly) {
        this(name, value, path, domain, DEFAULT_VERSION, comment, maxAge, null, secure, httpOnly, null);
    }

    @Deprecated
    public NewCookie(final String name,
            final String value,
            final String path,
            final String domain,
            final int version,
            final String comment,
            final int maxAge,
            final boolean secure) {
        this(name, value, path, domain, version, comment, maxAge, null, secure, false, null);
    }

    @Deprecated
    public NewCookie(final String name,
            final String value,
            final String path,
            final String domain,
            final int version,
            final String comment,
            final int maxAge,
            final Date expiry,
            final boolean secure,
            final boolean httpOnly) {
        this(name, value, path, domain, version, comment, maxAge, expiry, secure, httpOnly, null);
    }

    @Deprecated
    public NewCookie(final String name,
            final String value,
            final String path,
            final String domain,
            final int version,
            final String comment,
            final int maxAge,
            final Date expiry,
            final boolean secure,
            final boolean httpOnly,
            final SameSite sameSite) {
        super(name, value, path, domain, version);
        this.comment = comment;
        this.maxAge = maxAge;
        this.expiry = expiry;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
    }

    @Deprecated
    public NewCookie(final Cookie cookie) {
        this(cookie, null, DEFAULT_MAX_AGE, null, false, false, null);
    }

    @Deprecated
    public NewCookie(final Cookie cookie, final String comment, final int maxAge, final boolean secure) {
        this(cookie, comment, maxAge, null, secure, false, null);
    }

    @Deprecated
    public NewCookie(final Cookie cookie, final String comment, final int maxAge, final Date expiry, final boolean secure, final boolean httpOnly) {
        this(cookie, comment, maxAge, expiry, secure, httpOnly, null);
    }

    @Deprecated
    public NewCookie(final Cookie cookie, final String comment, final int maxAge, final Date expiry, final boolean secure, final boolean httpOnly,
            final SameSite sameSite) {
        super(cookie == null ? null : cookie.getName(),
                cookie == null ? null : cookie.getValue(),
                cookie == null ? null : cookie.getPath(),
                cookie == null ? null : cookie.getDomain(),
                cookie == null ? Cookie.DEFAULT_VERSION : cookie.getVersion());
        this.comment = comment;
        this.maxAge = maxAge;
        this.expiry = expiry;
        this.secure = secure;
        this.httpOnly = httpOnly;
        this.sameSite = sameSite;
    }

    protected NewCookie(AbstractNewCookieBuilder<?> builder) {
        super(builder);
        this.comment = builder.comment;
        this.maxAge = builder.maxAge;
        this.expiry = builder.expiry;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
        this.sameSite = builder.sameSite;
    }

    @Deprecated
    public static NewCookie valueOf(final String value) {
        return DELEGATE.fromString(value);
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

    public SameSite getSameSite() {
        return sameSite;
    }

    public Cookie toCookie() {
        return new Cookie(this.getName(), this.getValue(), this.getPath(),
                this.getDomain(), this.getVersion());
    }

    @Override
    @Deprecated
    public String toString() {
        return DELEGATE.toString(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue(), getVersion(), getPath(), getDomain(),
                comment, maxAge, expiry, secure, httpOnly, sameSite);
    }

    @SuppressWarnings({ "StringEquality", "RedundantIfStatement" })
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NewCookie other = (NewCookie) obj;
        if (!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.getValue(), other.getValue())) {
            return false;
        }
        if (this.getVersion() != other.getVersion()) {
            return false;
        }
        if (!Objects.equals(this.getPath(), other.getPath())) {
            return false;
        }
        if (!Objects.equals(this.getDomain(), other.getDomain())) {
            return false;
        }
        if (!Objects.equals(this.comment, other.comment)) {
            return false;
        }
        if (this.maxAge != other.maxAge) {
            return false;
        }

        if (!Objects.equals(this.expiry, other.expiry)) {
            return false;
        }

        if (this.secure != other.secure) {
            return false;
        }
        if (this.httpOnly != other.httpOnly) {
            return false;
        }
        if (this.sameSite != other.sameSite) {
            return false;
        }
        return true;
    }

    public enum SameSite {
        NONE,
        LAX,
        STRICT
    }

    public static class Builder extends AbstractNewCookieBuilder<Builder> {

        public Builder(String name) {
            super(name);
        }

        public Builder(Cookie cookie) {
            super(cookie);
        }

        @Override
        public NewCookie build() {
            return new NewCookie(this);
        }
    }

    public abstract static class AbstractNewCookieBuilder<T extends AbstractNewCookieBuilder<T>> extends AbstractCookieBuilder<AbstractNewCookieBuilder<T>> {

        private String comment;
        private int maxAge = DEFAULT_MAX_AGE;
        private Date expiry;
        private boolean secure;
        private boolean httpOnly;
        private SameSite sameSite;

        public AbstractNewCookieBuilder(String name) {
            super(name);
        }

        public AbstractNewCookieBuilder(Cookie cookie) {
            super(cookie == null ? null : cookie.getName());
            if (cookie != null) {
                value(cookie.getValue());
                path(cookie.getPath());
                domain(cookie.getDomain());
                version(cookie.getVersion());
            }
        }

        public T comment(String comment) {
            this.comment = comment;
            return self();
        }

        public T maxAge(int maxAge) {
            this.maxAge = maxAge;
            return self();
        }

        public T expiry(Date expiry) {
            this.expiry = expiry;
            return self();
        }

        public T secure(boolean secure) {
            this.secure = secure;
            return self();
        }

        public T httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return self();
        }

        public T sameSite(SameSite sameSite) {
            this.sameSite = sameSite;
            return self();
        }

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        @Override
        public abstract NewCookie build();
    }
}

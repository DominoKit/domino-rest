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

import java.util.Objects;

public class Cookie {

    public static final int DEFAULT_VERSION = 1;

    private final String name;
    private final String value;
    private final int version;
    private final String path;
    private final String domain;

    @Deprecated
    public Cookie(final String name, final String value, final String path, final String domain, final int version)
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

    @Deprecated
    public Cookie(final String name, final String value, final String path, final String domain)
            throws IllegalArgumentException {
        this(name, value, path, domain, DEFAULT_VERSION);
    }

    @Deprecated
    public Cookie(final String name, final String value)
            throws IllegalArgumentException {
        this(name, value, null, null);
    }

    protected Cookie(AbstractCookieBuilder<?> builder) throws IllegalArgumentException {
        if (builder.name == null) {
            throw new IllegalArgumentException("name==null");
        }
        this.name = builder.name;
        this.value = builder.value;
        this.version = builder.version;
        this.domain = builder.domain;
        this.path = builder.path;
    }

    @Deprecated
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
    @Deprecated
    public String toString() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.value, this.version, this.path, this.domain);
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
        final Cookie other = (Cookie) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.domain, other.domain)) {
            return false;
        }
        return true;
    }

    public static class Builder extends AbstractCookieBuilder<Builder> {

        public Builder(String name) {
            super(name);
        }

        @Override
        public Cookie build() {
            return new Cookie(this);
        }
    }

    public abstract static class AbstractCookieBuilder<T extends AbstractCookieBuilder<T>> {

        private final String name;

        private String value;
        private int version = DEFAULT_VERSION;
        private String path;
        private String domain;

        public AbstractCookieBuilder(String name) {
            this.name = name;
        }

        public T value(String value) {
            this.value = value;
            return self();
        }

        public T version(int version) {
            this.version = version;
            return self();
        }

        public T path(String path) {
            this.path = path;
            return self();
        }

        public T domain(String domain) {
            this.domain = domain;
            return self();
        }

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public abstract Cookie build();
    }
}

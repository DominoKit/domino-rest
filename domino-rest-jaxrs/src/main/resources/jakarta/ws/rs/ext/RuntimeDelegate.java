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

package jakarta.ws.rs.ext;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant.VariantListBuilder;

public abstract class RuntimeDelegate {

    public static final String JAXRS_RUNTIME_DELEGATE_PROPERTY = "jakarta.ws.rs.ext.RuntimeDelegate";
    private static final Object RD_LOCK = new Object();
    private static volatile RuntimeDelegate cachedDelegate;

    protected RuntimeDelegate() {
    }

    public static RuntimeDelegate getInstance() {
       return new RuntimeDelegate(){
           @Override
           public UriBuilder createUriBuilder() {
               return null;
           }

           @Override
           public VariantListBuilder createVariantListBuilder() {
               return null;
           }

           @Override
           public <T> T createEndpoint(Application application, Class<T> endpointType)
               throws IllegalArgumentException, UnsupportedOperationException {
               return null;
           }

           @Override
           public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
               throws IllegalArgumentException {
               return null;
           }

           @Override
           public Link.Builder createLinkBuilder() {
               return null;
           }
       };
    }

    public static void setInstance(final RuntimeDelegate rd) {
    }

    public abstract UriBuilder createUriBuilder();

    public abstract VariantListBuilder createVariantListBuilder();

    public abstract <T> T createEndpoint(Application application, Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException;

    public abstract <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
            throws IllegalArgumentException;

    public static interface HeaderDelegate<T> {

        public T fromString(String value);

        public String toString(T value);
    }

    public abstract Link.Builder createLinkBuilder();
}

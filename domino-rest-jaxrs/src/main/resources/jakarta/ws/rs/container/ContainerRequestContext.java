/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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
package jakarta.ws.rs.container;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.GwtIncompatible;

public interface ContainerRequestContext {

    public Object getProperty(String name);

    public default boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public Collection<String> getPropertyNames();

    public void setProperty(String name, Object object);

    public void removeProperty(String name);

    public UriInfo getUriInfo();

    @GwtIncompatible
    public void setRequestUri(URI requestUri);

    @GwtIncompatible
    public void setRequestUri(URI baseUri, URI requestUri);

    public Request getRequest();

    public String getMethod();

    public void setMethod(String method);

    public MultivaluedMap<String, String> getHeaders();

    public String getHeaderString(String name);

    public boolean containsHeaderString(String name, String valueSeparatorRegex, Predicate<String> valuePredicate);

    public default boolean containsHeaderString(String name, Predicate<String> valuePredicate) {
        return containsHeaderString(name, ",", valuePredicate);
    }

    public Date getDate();

    @GwtIncompatible
    public Locale getLanguage();

    public int getLength();

    public MediaType getMediaType();

    @GwtIncompatible
    public List<MediaType> getAcceptableMediaTypes();

    public List<Locale> getAcceptableLanguages();

    public Map<String, Cookie> getCookies();

    public boolean hasEntity();

    @GwtIncompatible
    public InputStream getEntityStream();

    @GwtIncompatible
    public void setEntityStream(InputStream input);

    public SecurityContext getSecurityContext();

    public void setSecurityContext(SecurityContext context);

    public void abortWith(Response response);
}

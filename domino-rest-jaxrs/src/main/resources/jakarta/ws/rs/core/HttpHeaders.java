/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.ws.rs.GwtIncompatible;
import java.util.function.Predicate;

public interface HttpHeaders {

    public List<String> getRequestHeader(String name);

    public String getHeaderString(String name);

    public boolean containsHeaderString(String name, String valueSeparatorRegex, Predicate<String> valuePredicate);

    public default boolean containsHeaderString(String name, Predicate<String> valuePredicate) {
        return containsHeaderString(name, ",", valuePredicate);
    }

    public MultivaluedMap<String, String> getRequestHeaders();

    public List<MediaType> getAcceptableMediaTypes();

    @GwtIncompatible
    public List<Locale> getAcceptableLanguages();

    public MediaType getMediaType();

    @GwtIncompatible
    public Locale getLanguage();

    public Map<String, Cookie> getCookies();

    public Date getDate();

    public int getLength();

    public static final String ACCEPT = "Accept";

    public static final String ACCEPT_CHARSET = "Accept-Charset";

    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static final String ACCEPT_LANGUAGE = "Accept-Language";

    public static final String ALLOW = "Allow";

    public static final String AUTHORIZATION = "Authorization";

    public static final String CACHE_CONTROL = "Cache-Control";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String CONTENT_ENCODING = "Content-Encoding";

    public static final String CONTENT_ID = "Content-ID";

    public static final String CONTENT_LANGUAGE = "Content-Language";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String CONTENT_LOCATION = "Content-Location";

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String DATE = "Date";

    public static final String ETAG = "ETag";

    public static final String EXPECT = "Expect";

    public static final String EXPIRES = "Expires";

    public static final String HOST = "Host";

    public static final String IF_MATCH = "If-Match";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String IF_NONE_MATCH = "If-None-Match";

    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String LOCATION = "Location";

    public static final String LINK = "Link";

    public static final String RETRY_AFTER = "Retry-After";

    public static final String USER_AGENT = "User-Agent";

    public static final String VARY = "Vary";

    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    public static final String COOKIE = "Cookie";

    public static final String SET_COOKIE = "Set-Cookie";

    public static final String LAST_EVENT_ID_HEADER = "Last-Event-ID";
}

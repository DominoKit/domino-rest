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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import jakarta.ws.rs.GwtIncompatible;

import jakarta.ws.rs.ext.RuntimeDelegate;

@SuppressWarnings("JavaDoc")
public class MediaType {

    private final String type;
    private final String subtype;
    private final Map<String, String> parameters;
    private final int hash;

    public static final String CHARSET_PARAMETER = "charset";

    public static final String MEDIA_TYPE_WILDCARD = "*";
    // Common media type constants

    public static final String WILDCARD = "*";
    public static final MediaType WILDCARD_TYPE = new MediaType();

    public static final String APPLICATION_XML = "application/xml";

    public static final MediaType APPLICATION_XML_TYPE = new MediaType("application", "xml");

    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    public static final MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application", "atom+xml");

    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    public static final MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application", "xhtml+xml");

    @Deprecated
    public static final String APPLICATION_SVG_XML = "application/svg+xml";

    @Deprecated
    public static final MediaType APPLICATION_SVG_XML_TYPE = new MediaType("application", "svg+xml");

    public static final String APPLICATION_JSON = "application/json";

    public static final MediaType APPLICATION_JSON_TYPE = new MediaType("application", "json");

    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    public static final MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application", "x-www-form-urlencoded");

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public static final MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart", "form-data");

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application", "octet-stream");

    public static final String TEXT_PLAIN = "text/plain";

    public static final MediaType TEXT_PLAIN_TYPE = new MediaType("text", "plain");

    public static final String TEXT_XML = "text/xml";

    public static final MediaType TEXT_XML_TYPE = new MediaType("text", "xml");

    public static final String TEXT_HTML = "text/html";

    public static final MediaType TEXT_HTML_TYPE = new MediaType("text", "html");

    public static final String SERVER_SENT_EVENTS = "text/event-stream";

    public static final MediaType SERVER_SENT_EVENTS_TYPE = new MediaType("text", "event-stream");

    public static final String APPLICATION_JSON_PATCH_JSON = "application/json-patch+json";

    public static final MediaType APPLICATION_JSON_PATCH_JSON_TYPE = new MediaType("application", "json-patch+json");

    @GwtIncompatible
    public static MediaType valueOf(final String type) {
        return RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).fromString(type);
    }

    private static TreeMap<String, String> createParametersMap(final Map<String, String> initialValues) {
        final TreeMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (initialValues != null) {
            for (Map.Entry<String, String> e : initialValues.entrySet()) {
                map.put(e.getKey().toLowerCase(), e.getValue());
            }
        }
        return map;
    }

    public MediaType(final String type, final String subtype, final Map<String, String> parameters) {
        this(type, subtype, null, createParametersMap(parameters));
    }

    public MediaType(final String type, final String subtype) {
        this(type, subtype, null, null);
    }

    public MediaType(final String type, final String subtype, final String charset) {
        this(type, subtype, charset, null);
    }

    public MediaType() {
        this(MEDIA_TYPE_WILDCARD, MEDIA_TYPE_WILDCARD, null, null);
    }

    private MediaType(final String type, final String subtype, final String charset, final Map<String, String> parameterMap) {

        this.type = type == null ? MEDIA_TYPE_WILDCARD : type;
        this.subtype = subtype == null ? MEDIA_TYPE_WILDCARD : subtype;

        Map<String, String> map = parameterMap;
        if (map == null) {
            map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        }

        if (charset != null && !charset.isEmpty()) {
            map.put(CHARSET_PARAMETER, charset);
        }
        this.parameters = Collections.unmodifiableMap(map);
        this.hash = Objects.hash(this.type.toLowerCase(), this.subtype.toLowerCase(), this.parameters);
    }

    public String getType() {
        return this.type;
    }

    public boolean isWildcardType() {
        return this.getType().equals(MEDIA_TYPE_WILDCARD);
    }

    public String getSubtype() {
        return this.subtype;
    }

    public boolean isWildcardSubtype() {
        return this.getSubtype().equals(MEDIA_TYPE_WILDCARD);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public MediaType withCharset(final String charset) {
        return new MediaType(this.type, this.subtype, charset, createParametersMap(this.parameters));
    }

    public boolean isCompatible(final MediaType other) {
        if (other == null) {
            return false;
        }

        return
            (type.equalsIgnoreCase(other.type) || this.isWildcardType() || other.isWildcardType())
            &&
            (subtype.equalsIgnoreCase(other.subtype) || this.isWildcardSubtype()
             || other.isWildcardSubtype());
    }

    @SuppressWarnings("UnnecessaryJavaDocLink")
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MediaType)) {
            return false;
        }

        MediaType other = (MediaType) obj;
        return (this.type.equalsIgnoreCase(other.type)
                && this.subtype.equalsIgnoreCase(other.subtype)
                && this.parameters.equals(other.parameters));
    }

    @SuppressWarnings("UnnecessaryJavaDocLink")
    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public String toString() {
        return RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class).toString(this);
    }
}

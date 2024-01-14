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

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.ws.rs.GwtIncompatible;

public abstract class Response implements AutoCloseable {

    protected Response() {
    }

    public abstract int getStatus();

    public abstract StatusType getStatusInfo();
    
    public abstract Object getEntity();

    public abstract <T> T readEntity(Class<T> entityType);

    @GwtIncompatible
    public abstract <T> T readEntity(GenericType<T> entityType);

    public abstract <T> T readEntity(Class<T> entityType, Annotation[] annotations);

    @GwtIncompatible
    public abstract <T> T readEntity(GenericType<T> entityType, Annotation[] annotations);
    
    public abstract boolean hasEntity();
    
    public abstract boolean bufferEntity();

    @Override
    public abstract void close();
    
    public abstract MediaType getMediaType();
    
    public abstract Locale getLanguage();

    public abstract int getLength();

    public abstract Set<String> getAllowedMethods();
    
    public abstract Map<String, NewCookie> getCookies();

    public abstract EntityTag getEntityTag();

    public abstract Date getDate();
    
    public abstract Date getLastModified();

    @GwtIncompatible
    public abstract URI getLocation();

    public abstract Set<Link> getLinks();

    public abstract boolean hasLink(String relation);
    
    public abstract Link getLink(String relation);
    
    public abstract Link.Builder getLinkBuilder(String relation);
    
    public abstract MultivaluedMap<String, Object> getMetadata();

    public MultivaluedMap<String, Object> getHeaders() {
        return getMetadata();
    }

    public abstract MultivaluedMap<String, String> getStringHeaders();
    
    public abstract String getHeaderString(String name);
    
    public boolean isClosed() {
        try {
            hasEntity();
            return false;
        } catch (IllegalStateException ignored) {
            return true;
        }
    }
    
    public interface StatusType {
        
        public int getStatusCode();
        
        public Status.Family getFamily();
        
        public String getReasonPhrase();

        public default Status toEnum() {
            return Status.fromStatusCode(getStatusCode());
        }
    }

    public enum Status implements StatusType {

        OK(200, "OK"),

        CREATED(201, "Created"),
        
        ACCEPTED(202, "Accepted"),
        
        NO_CONTENT(204, "No Content"),
        
        RESET_CONTENT(205, "Reset Content"),
        
        PARTIAL_CONTENT(206, "Partial Content"),
        
        MULTIPLE_CHOICES(300, "Multiple Choices"),
        
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        
        FOUND(302, "Found"),
        
        SEE_OTHER(303, "See Other"),
        
        NOT_MODIFIED(304, "Not Modified"),
        
        USE_PROXY(305, "Use Proxy"),
        
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        
        PERMANENT_REDIRECT(308, "Permanent Redirect"),
        
        BAD_REQUEST(400, "Bad Request"),
        
        UNAUTHORIZED(401, "Unauthorized"),
        
        PAYMENT_REQUIRED(402, "Payment Required"),
        
        FORBIDDEN(403, "Forbidden"),
        
        NOT_FOUND(404, "Not Found"),
        
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
        
        REQUEST_TIMEOUT(408, "Request Timeout"),
        
        CONFLICT(409, "Conflict"),
        
        GONE(410, "Gone"),
        
        LENGTH_REQUIRED(411, "Length Required"),
        
        PRECONDITION_FAILED(412, "Precondition Failed"),
        
        REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
        
        REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
        
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
        
        REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
        
        EXPECTATION_FAILED(417, "Expectation Failed"),
        
        PRECONDITION_REQUIRED(428, "Precondition Required"),
        
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        
        REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
        
        UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),
        
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        
        NOT_IMPLEMENTED(501, "Not Implemented"),
        
        BAD_GATEWAY(502, "Bad Gateway"),
        
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        
        GATEWAY_TIMEOUT(504, "Gateway Timeout"),
        
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
        
        NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

        private final int code;
        private final String reason;
        private final Family family;
        
        public enum Family {
            
            INFORMATIONAL,
            
            SUCCESSFUL,
            
            REDIRECTION,
            
            CLIENT_ERROR,
            
            SERVER_ERROR,
            
            OTHER;
            
            public static Family familyOf(final int statusCode) {
                switch (statusCode / 100) {
                case 1:
                    return Family.INFORMATIONAL;
                case 2:
                    return Family.SUCCESSFUL;
                case 3:
                    return Family.REDIRECTION;
                case 4:
                    return Family.CLIENT_ERROR;
                case 5:
                    return Family.SERVER_ERROR;
                default:
                    return Family.OTHER;
                }
            }
        }

        Status(final int statusCode, final String reasonPhrase) {
            this.code = statusCode;
            this.reason = reasonPhrase;
            this.family = Family.familyOf(statusCode);
        }

        @Override
        public Family getFamily() {
            return family;
        }

        @Override
        public int getStatusCode() {
            return code;
        }

        @Override
        public String getReasonPhrase() {
            return toString();
        }

        @Override
        public String toString() {
            return reason;
        }
        
        public static Status fromStatusCode(final int statusCode) {
            for (Status s : Status.values()) {
                if (s.code == statusCode) {
                    return s;
                }
            }
            return null;
        }
    }
}

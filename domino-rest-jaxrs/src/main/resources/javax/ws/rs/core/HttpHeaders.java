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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.GwtIncompatible;


public interface HttpHeaders {

  
  public List<String> getRequestHeader(String name);

  
  public String getHeaderString(String name);

  
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

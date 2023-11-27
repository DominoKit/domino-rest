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

import java.util.List;
import javax.ws.rs.GwtIncompatible;


public interface UriInfo {

  
  public String getPath();

  
  public String getPath(boolean decode);

  
  public List<PathSegment> getPathSegments();

  
  public List<PathSegment> getPathSegments(boolean decode);


  
  public UriBuilder getRequestUriBuilder();


  
  public UriBuilder getAbsolutePathBuilder();


  
  public UriBuilder getBaseUriBuilder();

  
  public MultivaluedMap<String, String> getPathParameters();

  
  public MultivaluedMap<String, String> getPathParameters(boolean decode);

  
  public MultivaluedMap<String, String> getQueryParameters();

  
  public MultivaluedMap<String, String> getQueryParameters(boolean decode);

  
  public List<String> getMatchedURIs();

  
  public List<String> getMatchedURIs(boolean decode);

  
  public List<Object> getMatchedResources();
}

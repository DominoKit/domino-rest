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


package javax.ws.rs.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.ws.rs.GwtIncompatible;
import javax.ws.rs.core.MediaType;


public interface InterceptorContext {

  
  public Object getProperty(String name);

  
  public Collection<String> getPropertyNames();

  
  public void setProperty(String name, Object object);

  
  public void removeProperty(String name);

  @GwtIncompatible
  public Annotation[] getAnnotations();

  @GwtIncompatible
  public void setAnnotations(Annotation[] annotations);

  
  Class<?> getType();

  
  public void setType(Class<?> type);

  @GwtIncompatible
  Type getGenericType();

  @GwtIncompatible
  public void setGenericType(Type genericType);

  
  public MediaType getMediaType();

  
  public void setMediaType(MediaType mediaType);
}

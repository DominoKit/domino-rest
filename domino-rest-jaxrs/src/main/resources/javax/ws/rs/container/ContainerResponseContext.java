/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.ws.rs.container;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.GwtIncompatible;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface ContainerResponseContext {

  public int getStatus();

  public void setStatus(int code);

  public Response.StatusType getStatusInfo();
  public void setStatusInfo(Response.StatusType statusInfo);

  public MultivaluedMap<String, Object> getHeaders();

  public abstract MultivaluedMap<String, String> getStringHeaders();

  public String getHeaderString(String name);
  public Set<String> getAllowedMethods();
  public Date getDate();
  @GwtIncompatible
  public Locale getLanguage();

  public int getLength();

  public MediaType getMediaType();

  public Map<String, NewCookie> getCookies();

  public EntityTag getEntityTag();

  public Date getLastModified();

  @GwtIncompatible
  public URI getLocation();
  public Set<Link> getLinks();

  boolean hasLink(String relation);

  public Link getLink(String relation);

  public Link.Builder getLinkBuilder(String relation);

  public boolean hasEntity();
  public Object getEntity();

  public Class<?> getEntityClass();
  @GwtIncompatible
  public Type getEntityType();

  public void setEntity(final Object entity);

  @GwtIncompatible
  public void setEntity(
      final Object entity, final Annotation[] annotations, final MediaType mediaType);

  @GwtIncompatible
  public Annotation[] getEntityAnnotations();

  @GwtIncompatible
  public OutputStream getEntityStream();

  @GwtIncompatible
  public void setEntityStream(OutputStream outputStream);
}

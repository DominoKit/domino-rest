/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.GwtIncompatible;

public interface InterceptorContext {

    public Object getProperty(String name);

    public default boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

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

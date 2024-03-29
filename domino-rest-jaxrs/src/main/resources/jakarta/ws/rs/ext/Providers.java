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

package jakarta.ws.rs.ext;

import jakarta.ws.rs.GwtIncompatible;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.core.MediaType;

@GwtIncompatible
public interface Providers {

    <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
            Type genericType, Annotation[] annotations, MediaType mediaType);

    <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
            Type genericType, Annotation[] annotations, MediaType mediaType);

    <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type);

    <T> ContextResolver<T> getContextResolver(Class<T> contextType,
            MediaType mediaType);
}

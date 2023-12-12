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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import jakarta.ws.rs.GwtIncompatible;

/**
 * Used for emulating Context in domino-rest, arguments of this type will be ignored in domino-rest
 * client generation
 */
public interface AsyncResponse {

    @GwtIncompatible long NO_TIMEOUT = 0;

    @GwtIncompatible boolean resume(Object response);

    @GwtIncompatible boolean resume(Throwable response);

    @GwtIncompatible boolean cancel();

    @GwtIncompatible boolean cancel(int retryAfter);

    @GwtIncompatible boolean cancel(Date retryAfter);

    @GwtIncompatible boolean isSuspended();

    @GwtIncompatible boolean isCancelled();

    @GwtIncompatible boolean isDone();

    @GwtIncompatible boolean setTimeout(long time, TimeUnit unit);

    @GwtIncompatible void setTimeoutHandler(TimeoutHandler handler);

    @GwtIncompatible Collection<Class<?>> register(Class<?> callback);

    @GwtIncompatible Map<Class<?>, Collection<Class<?>>> register(Class<?> callback, Class<?>... callbacks);

    @GwtIncompatible Collection<Class<?>> register(Object callback);

    Map<Class<?>, Collection<Class<?>>> register(Object callback, Object... callbacks);
}

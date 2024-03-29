/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;

public interface Configurable<C extends Configurable> {

    public Configuration getConfiguration();

    public C property(String name, Object value);

    public C register(Class<?> componentClass);

    public C register(Class<?> componentClass, int priority);

    public C register(Class<?> componentClass, Class<?>... contracts);

    public C register(Class<?> componentClass, Map<Class<?>, Integer> contracts);

    public C register(Object component);

    public C register(Object component, int priority);

    public C register(Object component, Class<?>... contracts);

    public C register(Object component, Map<Class<?>, Integer> contracts);
}

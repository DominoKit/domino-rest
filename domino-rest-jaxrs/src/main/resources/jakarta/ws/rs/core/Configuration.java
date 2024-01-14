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

package jakarta.ws.rs.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.RuntimeType;


public interface Configuration {

    public RuntimeType getRuntimeType();

    public Map<String, Object> getProperties();

    public Object getProperty(String name);

    public default boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public Collection<String> getPropertyNames();

    public boolean isEnabled(Feature feature);

    public boolean isEnabled(Class<? extends Feature> featureClass);

    public boolean isRegistered(Object component);

    public boolean isRegistered(Class<?> componentClass);

    public Map<Class<?>, Integer> getContracts(Class<?> componentClass);

    public Set<Class<?>> getClasses();

    public Set<Object> getInstances();
}

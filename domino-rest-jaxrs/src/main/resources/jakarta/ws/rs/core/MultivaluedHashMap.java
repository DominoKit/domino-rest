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

package jakarta.ws.rs.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultivaluedHashMap<K, V> extends AbstractMultivaluedMap<K, V> implements Serializable {

    private static final long serialVersionUID = -6052320403766368902L;

    public MultivaluedHashMap() {
        super(new HashMap<K, List<V>>());
    }

    public MultivaluedHashMap(final int initialCapacity) {
        super(new HashMap<K, List<V>>(initialCapacity));
    }

    public MultivaluedHashMap(final int initialCapacity, final float loadFactor) {
        super(new HashMap<K, List<V>>(initialCapacity, loadFactor));
    }

    public MultivaluedHashMap(final MultivaluedMap<? extends K, ? extends V> map) {
        this();
        putAll(map);
    }

    private <T extends K, U extends V> void putAll(final MultivaluedMap<T, U> map) {
        for (Entry<T, List<U>> e : map.entrySet()) {
            store.put(e.getKey(), new ArrayList<V>(e.getValue()));
        }
    }

    public MultivaluedHashMap(final Map<? extends K, ? extends V> map) {
        this();
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            this.putSingle(e.getKey(), e.getValue());
        }
    }
}

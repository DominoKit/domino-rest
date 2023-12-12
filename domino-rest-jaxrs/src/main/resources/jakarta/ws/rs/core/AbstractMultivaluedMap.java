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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMultivaluedMap<K, V> implements MultivaluedMap<K, V>, Serializable {

    protected final Map<K, List<V>> store;

    public AbstractMultivaluedMap(final Map<K, List<V>> store) {
        if (store == null) {
            throw new NullPointerException("Underlying store must not be 'null'.");
        }
        this.store = store;
    }

    @Override
    public final void putSingle(final K key, final V value) {
        List<V> values = getValues(key);

        values.clear();
        if (value != null) {
            values.add(value);
        } else {
            addNull(values);
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected void addNull(final List<V> values) {
        // do nothing in the default implementation; ignore the null value
    }

    @SuppressWarnings("UnusedParameters")
    protected void addFirstNull(final List<V> values) {
        // do nothing in the default implementation; ignore the null value
    }

    @Override
    public final void add(final K key, final V value) {
        List<V> values = getValues(key);

        if (value != null) {
            values.add(value);
        } else {
            addNull(values);
        }
    }

    @Override
    public final void addAll(final K key, final V... newValues) {
        if (newValues == null) {
            throw new NullPointerException("Supplied array of values must not be null.");
        }
        if (newValues.length == 0) {
            return;
        }

        List<V> values = getValues(key);

        for (V value : newValues) {
            if (value != null) {
                values.add(value);
            } else {
                addNull(values);
            }
        }
    }

    @Override
    public final void addAll(final K key, final List<V> valueList) {
        if (valueList == null) {
            throw new NullPointerException("Supplied list of values must not be null.");
        }
        if (valueList.isEmpty()) {
            return;
        }

        List<V> values = getValues(key);

        for (V value : valueList) {
            if (value != null) {
                values.add(value);
            } else {
                addNull(values);
            }
        }
    }

    @Override
    public final V getFirst(final K key) {
        List<V> values = store.get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }

    @Override
    public final void addFirst(final K key, final V value) {
        List<V> values = getValues(key);

        if (value != null) {
            values.add(0, value);
        } else {
            addFirstNull(values);
        }
    }

    protected final List<V> getValues(final K key) {
        List<V> l = store.get(key);
        if (l == null) {
            l = new LinkedList<V>();
            store.put(key, l);
        }
        return l;
    }

    @Override
    public String toString() {
        return store.toString();
    }

    @Override
    public int hashCode() {
        return store.hashCode();
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(final Object o) {
        return store.equals(o);
    }

    @Override
    public Collection<List<V>> values() {
        return store.values();
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public List<V> remove(final Object key) {
        return store.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends List<V>> m) {
        store.putAll(m);
    }

    @Override
    public List<V> put(final K key, final List<V> value) {
        return store.put(key, value);
    }

    @Override
    public Set<K> keySet() {
        return store.keySet();
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public List<V> get(final Object key) {
        return store.get(key);
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return store.entrySet();
    }

    @Override
    public boolean containsValue(final Object value) {
        return store.containsValue(value);
    }

    @Override
    public boolean containsKey(final Object key) {
        return store.containsKey(key);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public boolean equalsIgnoreValueOrder(final MultivaluedMap<K, V> omap) {
        if (this == omap) {
            return true;
        }
        if (!keySet().equals(omap.keySet())) {
            return false;
        }
        for (Entry<K, List<V>> e : entrySet()) {
            List<V> olist = omap.get(e.getKey());
            if (e.getValue().size() != olist.size()) {
                return false;
            }
            for (V v : e.getValue()) {
                if (!olist.contains(v)) {
                    return false;
                }
            }
        }
        return true;
    }
}

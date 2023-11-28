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

  
  public MultivaluedHashMap(int initialCapacity) {
    super(new HashMap<K, List<V>>(initialCapacity));
  }

  
  public MultivaluedHashMap(int initialCapacity, float loadFactor) {
    super(new HashMap<K, List<V>>(initialCapacity, loadFactor));
  }

  
  public MultivaluedHashMap(MultivaluedMap<? extends K, ? extends V> map) {
    this();
    putAll(map);
  }

  
  private <T extends K, U extends V> void putAll(MultivaluedMap<T, U> map) {
    for (Entry<T, List<U>> e : map.entrySet()) {
      store.put(e.getKey(), new ArrayList<V>(e.getValue()));
    }
  }

  
  public MultivaluedHashMap(Map<? extends K, ? extends V> map) {
    this();
    for (Entry<? extends K, ? extends V> e : map.entrySet()) {
      this.putSingle(e.getKey(), e.getValue());
    }
  }
}

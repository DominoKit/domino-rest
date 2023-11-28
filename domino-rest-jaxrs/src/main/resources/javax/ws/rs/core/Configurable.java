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

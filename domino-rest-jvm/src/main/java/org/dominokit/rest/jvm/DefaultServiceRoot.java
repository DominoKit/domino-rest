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
package org.dominokit.rest.jvm;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.dominokit.rest.VertxInstanceProvider;

/** Helper class that is used to load {@link VertxInstanceProvider} using {@link ServiceLoader} */
public class DefaultServiceRoot {

  /** @return the service root based on the {@link VertxInstanceProvider} exists in the classpath */
  public static String get() {
    Iterator<VertxInstanceProvider> iterator =
        ServiceLoader.load(VertxInstanceProvider.class).iterator();
    VertxInstanceProvider provider;
    if (iterator.hasNext()) {
      provider = iterator.next();
    } else {
      provider = new DefaultProvider();
    }

    String protocol = provider.getProtocol();
    String host = provider.getHost();
    int port = provider.getPort();

    return protocol + "://" + host + ":" + port + "/";
  }
}

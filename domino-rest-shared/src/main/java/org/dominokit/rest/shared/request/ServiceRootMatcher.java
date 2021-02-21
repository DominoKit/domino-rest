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
package org.dominokit.rest.shared.request;

import java.util.List;

/**
 * Helper class to check if the path's service root configured using {@link DynamicServiceRoot}
 *
 * @see DynamicServiceRoot
 */
public class ServiceRootMatcher {

  private static final DynamicServiceRoot defaultRoot =
      DynamicServiceRoot.pathMatcher(path -> true)
          .serviceRoot(() -> DominoRestContext.make().getConfig().getDefaultServiceRoot());

  /**
   * @param path the path
   * @return the service root for a specific path
   */
  public static String matchedServiceRoot(String path) {
    final List<DynamicServiceRoot> serviceRoots =
        DominoRestContext.make().getConfig().getServiceRoots();
    return serviceRoots.stream()
        .filter(r -> r.isMatchingPath(path))
        .findFirst()
        .orElse(defaultRoot)
        .onMatchingPath(path);
  }

  /**
   * @param path the path
   * @return true if the path has a dynamic service root configured, false otherwise
   */
  public static boolean hasServiceRoot(String path) {
    final List<DynamicServiceRoot> serviceRoots =
        DominoRestContext.make().getConfig().getServiceRoots();
    return serviceRoots.stream().anyMatch(r -> r.isMatchingPath(path));
  }
}

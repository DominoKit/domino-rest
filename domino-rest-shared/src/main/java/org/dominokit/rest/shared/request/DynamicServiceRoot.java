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

/** A class to provide a dynamic service root based on path matching. */
public class DynamicServiceRoot implements HasPathMatcher {

  private final PathMatcher pathMatcher;
  private HasServiceRoot hasServiceRoot;
  private PathFormatter pathFormatter = (root, request) -> root + request.getPath();

  private DynamicServiceRoot(PathMatcher pathMatcher) {
    this.pathMatcher = pathMatcher;
  }

  public boolean isMatchingPath(ImmutableServerRequest<?, ?> serviceRoot) {
    return pathMatcher.isMatch(serviceRoot);
  }

  public String onMatchingPath(ImmutableServerRequest<?, ?> request) {
    String root = hasServiceRoot.onMatch();
    return pathFormatter.format(root, request);
  }

  public static DynamicServiceRoot pathMatcher(PathMatcher pathMatcher) {
    return new DynamicServiceRoot(pathMatcher);
  }

  @Override
  public DynamicServiceRoot serviceRoot(HasServiceRoot hasServiceRoot) {
    this.hasServiceRoot = hasServiceRoot;
    return this;
  }

  public DynamicServiceRoot pathFormatter(PathFormatter pathFormatter) {
    this.pathFormatter = pathFormatter;
    return this;
  }
}

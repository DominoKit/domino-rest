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
package org.dominokit.domino.rest.shared.request;

/**
 * Interface allows configuring service roots based on a specific path matcher.
 *
 * <p>The path matcher configures path formatter based on a matcher, if the path matches the
 * condition, then its path will formatted using the path formatter. This is helpful when loading
 * the services roots from configuration and then assigning them to specific paths using conditions.
 *
 * @see HasServiceRoot
 * @see PathFormatter
 * @see PathMatcher
 */
public interface HasPathMatcher {

  /**
   * Configure the new service root associated with this matcher
   *
   * @param serviceRoot the producer of the new service root
   * @return same instance to support builder pattern
   */
  HasPathMatcher serviceRoot(HasServiceRoot serviceRoot);

  /**
   * Formats the path associated with this matcher
   *
   * @param pathFormatter the producer of the new formatted path
   * @return same instance to support builder pattern
   */
  HasPathMatcher pathFormatter(PathFormatter pathFormatter);

  /** Producer to a new service root */
  @FunctionalInterface
  interface HasServiceRoot {
    /** @return the new service root */
    String onMatch();
  }

  /** Formatter of the root path based on the service root */
  @FunctionalInterface
  interface PathFormatter {
    /**
     * Formats the path
     *
     * @param root the root path
     * @param serviceRoot the service root associated with the path
     * @return the new formatted path
     */
    String format(String root, String serviceRoot);
  }

  /** Matcher which checks if a path matches the condition */
  @FunctionalInterface
  interface PathMatcher {
    /**
     * @param path the path to check
     * @return true if the path matches the condition, false otherwise
     */
    boolean isMatch(String path);
  }
}

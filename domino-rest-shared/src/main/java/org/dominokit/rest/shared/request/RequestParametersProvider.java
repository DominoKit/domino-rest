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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An interface for providing various parameters associated with a request. */
public interface RequestParametersProvider {
  /**
   * @return a map of request headers
   */
  default Map<String, String> getHeaders() {
    return new HashMap<>();
  }

  /**
   * @return a map of query parameters
   */
  default Map<String, List<String>> getQueryParameters() {
    return new HashMap<>();
  }

  /**
   * @return a map of path parameters
   */
  default Map<String, String> getPathParameters() {
    return new HashMap<>();
  }

  /**
   * @return a map of matrix parameters
   */
  default Map<String, List<String>> getMatrixParameters() {
    return new HashMap<>();
  }

  /**
   * @return a map of fragment parameters
   */
  default Map<String, String> getFragmentParameters() {
    return new HashMap<>();
  }

  /**
   * @return a map of meta parameters
   */
  default Map<String, MetaParam> getMetaParameters() {
    return new HashMap<>();
  }
}

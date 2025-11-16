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
package org.dominokit.rest.shared;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Representation of {@link RestfulRequest} response */
public interface Response {

  /**
   * @param header the name
   * @return response header value
   */
  List<String> getHeader(String header);

  /** @return all response headers */
  Map<String, List<String>> getHeaders();

  /** @return the status code of the response */
  int getStatusCode();

  /** @return the status of the response as text */
  String getStatusText();

  /** @return the body of the response as a {@link String} */
  String getBodyAsString();

  byte[] getBodyAsBytes();

  Optional<Object> getBean();

  void setBean(Object bean);
}

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
package org.dominokit.rest.shared.request.exception;

import org.dominokit.rest.shared.request.UrlFormatter;

/**
 * An exception is thrown when trying to replace query parameters when there is no expressions to
 * replace
 *
 * @see UrlFormatter
 */
public class QueryParameterMissingException extends RuntimeException {

  public QueryParameterMissingException(String paramName) {
    super("No parameter provided for query parameter [" + paramName + "]");
  }
}

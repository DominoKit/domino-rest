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
package org.dominokit.rest.auto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Triggers OpenAPI-based client generation.
 *
 * <p>The value points to an OpenAPI 3.1 definition placed on the classpath (typically under {@code
 * src/main/resources}). The annotation processor will read that file and emit JAX-RS interfaces
 * annotated with {@code @RequestFactory} based on tags and operationIds.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface RestAuto {

  /** Path to the OpenAPI document relative to the classpath. */
  String value();
}

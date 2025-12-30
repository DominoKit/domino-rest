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
package org.dominokit.rest.shared.request.service.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used when you have multiple methods defined in a service with the same name
 * but with different arguments.
 *
 * <p>This will help domino rest to generate the correct implementation based on the arguments
 * provided in each method.
 *
 * <p>For example:
 *
 * <pre>
 * &#64;RequestFactory
 * public interface SampleService {
 *     &#64;GET
 *     &#64;Path("someService/{id}")
 *     &#64;Classifier("long")
 *     void getById(Long id);
 *
 *     &#64;GET
 *     &#64;Path("someService/:id")
 *     &#64;Classifier("double")
 *     int getById(Double id);
 * }
 * </pre>
 *
 * @see RequestFactory
 * @see jakarta.ws.rs.GET
 * @see jakarta.ws.rs.Path
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Classifier {

  /**
   * @return the classifier value
   */
  String value() default "";
}

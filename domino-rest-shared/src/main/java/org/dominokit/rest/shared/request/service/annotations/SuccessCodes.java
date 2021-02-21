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
import org.dominokit.rest.shared.request.Fail;
import org.dominokit.rest.shared.request.ServerRequest;
import org.dominokit.rest.shared.request.Success;

/**
 * This annotation is used to define the success codes for this endpoint.
 *
 * <p>Domino rest will use these codes to determine if the request succeed or not in order to call
 * {@link ServerRequest#onSuccess(Success)} or {@link ServerRequest#onFailed(Fail)}
 *
 * @see ServerRequest
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SuccessCodes {
  int[] value() default {200, 201, 202, 203, 204};
}

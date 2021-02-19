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
package org.dominokit.domino.rest.shared.request.service.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.dominokit.domino.rest.shared.request.RequestBean;
import org.dominokit.jacksonapt.annotation.JSONMapper;

/**
 * This annotation is used to mark the request body to an endpoint defined inside a service.
 *
 * <p>Domino rest uses the following sequence to determine the request body for an endpoint:
 *
 * <ul>
 *   <li>if the method does have http method that does not require body, then the body will be
 *       ignored.
 *   <li>checks if there is an argument that may be considered as a request body using the following
 *       sequence:
 *       <ul>
 *         <li>if it is a class implements {@link RequestBean}
 *         <li>if it is annotated with {@link RequestBody}
 *         <li>if it is a class annotated with {@link RequestBody}
 *         <li>if it is a class annotated with {@link JSONMapper}
 *         <li>if none of the above matched, domino rest will consider the last un-annotated
 *             argument as a body
 *       </ul>
 * </ul>
 *
 * <p>
 *
 * <p>Once the request body for an endpoint is determined, domino rest will use
 * <b>domino-jackson</b> to serialize the body if there is no {@link Writer} defined for it.
 *
 * @see Writer
 * @see RequestBean
 * @see JSONMapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface RequestBody {}

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
import org.dominokit.rest.shared.request.RestConfig;

/**
 * Annotation can be used to mark the type a client to a REST service.
 *
 * <p>This annotation will be used to generate an implementation for accessing defined endpoints
 * according to the methods defined in this type. Each method is an endpoint with a specification
 * such as Http Method, URL, Request body if applicable and many more. The definition of an endpoint
 * is done using jax-rs annotations such as {@link jakarta.ws.rs.GET}, {@link jakarta.ws.rs.Path}
 * ... etc.
 *
 * <p>Domino rest loads the default service root from {@link RestConfig#getDefaultServiceRoot()}.
 * However, you can specifically define a base url for a specific client using {@link
 * RequestFactory#serviceRoot()}, this will override the service root for this client only.
 *
 * <p>For example:
 *
 * <pre>
 * &#64;RequestFactory(serviceRoot = "http://otherhost:9090")
 * public interface MoviesService {
 *
 *     &#64;jakarta.ws.rs.Path("library/movies/:movieName")
 *     &#64;jakarta.ws.rs.GET
 *     Movie getMovieByName(&#64;jakarta.ws.rs.PathParam("movieName") String movieName);
 *
 *     &#64;jakarta.ws.rs.Path("library/movies")
 *     &#64;jakarta.ws.rs.GET
 *     List&#60;Movie&#62; listMovies();
 * }
 * </pre>
 *
 * @see RestConfig
 * @see jakarta.ws.rs.Path
 * @see jakarta.ws.rs.GET
 * @see jakarta.ws.rs.PathParam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestFactory {

  /**
   * @return the service root for accessing these APIs, default to empty to load it from {@link
   *     RestConfig#getDefaultServiceRoot()}
   */
  String serviceRoot() default "";
}

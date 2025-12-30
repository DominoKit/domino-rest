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
import org.dominokit.rest.shared.request.ResponseReader;

/**
 * This annotation is used to provide reader for the content of the response.
 *
 * <p>Domino rest uses Domino-jackson internally to parse the content of the body in case it is a
 * JSON, this annotation is used to override this behaviour and implement the reading logic in a
 * different way.
 *
 * <p>For example:
 *
 * <pre>
 * &#64;RequestFactory
 * public interface MoviesService {
 *
 *     &#64;Path("library/movies/:movieName")
 *     &#64;GET
 *     &#64;Produce(MediaType.APPLICATION_XML)
 *     &#64;Reader(XmlMovieReader.class)
 *     Movie getMovieByName(@PathParam("movieName") String movieName);
 * }
 *
 * public class XmlMovieReader implements ResponseReader&#60;Movie&#62;{
 *     &#64;Override
 *     public Movie read(String request) {
 *         Movie movieFromXml = //convert the xml to Movie
 *         return movieFromXml;
 *     }
 * }
 * </pre>
 *
 * @see ResponseReader
 * @see jakarta.ws.rs.Produces
 * @see jakarta.ws.rs.GET
 * @see jakarta.ws.rs.Path
 * @see RequestFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reader {
  /**
   * @return the reader class
   */
  Class<? extends ResponseReader> value();
}

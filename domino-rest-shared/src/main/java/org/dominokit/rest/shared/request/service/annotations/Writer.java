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
import org.dominokit.rest.shared.request.RequestWriter;

/**
 * This annotation is used to provide writer for the request body.
 *
 * <p>Domino rest uses Domino-jackson internally to serialize the body in case it is a JSON, this
 * annotation is used to override this behaviour and implement the writing logic in a different way.
 *
 * <p>For example:
 *
 * <pre>
 * &#64;RequestFactory
 * public interface MoviesService {
 *
 *     &#64;Path("movies/:name")
 *     &#64;PUT
 *     &#64;Consumes(MediaType.APPLICATION_XML)
 *     &#64;Writer(XmlMovieWriter.class)
 *     void updateMovie(@BeanParam @RequestBody Movie movie);
 * }
 *
 * public class XmlMovieWriter implements RequestWriter&#60;Movie&#62;{
 *     &#64;Override
 *     public String read(Movie request) {
 *         String movieXml = //convert the movie to xml
 *         return movieXml;
 *     }
 * }
 * </pre>
 *
 * @see RequestWriter
 * @see javax.ws.rs.Consumes
 * @see javax.ws.rs.PUT
 * @see javax.ws.rs.Path
 * @see RequestFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Writer {
  Class<? extends RequestWriter> value();
}

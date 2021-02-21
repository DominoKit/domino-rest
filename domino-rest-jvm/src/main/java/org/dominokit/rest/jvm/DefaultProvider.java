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
package org.dominokit.rest.jvm;

import io.vertx.core.Vertx;
import org.dominokit.rest.VertxInstanceProvider;

/**
 * Default {@link VertxInstanceProvider} that creates a new Vert.x instance with default host
 * configuration
 */
public class DefaultProvider implements VertxInstanceProvider {
  /** {@inheritDoc} */
  @Override
  public Vertx getInstance() {
    return Vertx.vertx();
  }

  /** {@inheritDoc} */
  @Override
  public String getHost() {
    return "localhost";
  }

  /** {@inheritDoc} */
  @Override
  public int getPort() {
    return 8080;
  }

  /** {@inheritDoc} */
  @Override
  public String getProtocol() {
    return "http";
  }
}

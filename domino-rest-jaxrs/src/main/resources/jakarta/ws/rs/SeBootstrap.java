/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package jakarta.ws.rs;

import java.net.URI;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.core.UriBuilder;

public interface SeBootstrap {

    interface Configuration {

        String PROTOCOL = "jakarta.ws.rs.SeBootstrap.Protocol";

        String HOST = "jakarta.ws.rs.SeBootstrap.Host";

        String PORT = "jakarta.ws.rs.SeBootstrap.Port";

        String ROOT_PATH = "jakarta.ws.rs.SeBootstrap.RootPath";

        String SSL_CONTEXT = "jakarta.ws.rs.SeBootstrap.SSLContext";

        String SSL_CLIENT_AUTHENTICATION = "jakarta.ws.rs.SeBootstrap.SSLClientAuthentication";

        enum SSLClientAuthentication {

            NONE,

            OPTIONAL,

            MANDATORY
        }

        int FREE_PORT = 0;

        int DEFAULT_PORT = -1;

        Object property(String name);

        default boolean hasProperty(String name) {
            return property(name) != null;
        }

        default String protocol() {
            return (String) property(PROTOCOL);
        }

        default String host() {
            return (String) property(HOST);
        }

        default int port() {
            return (int) property(PORT);
        }

        default String rootPath() {
            return (String) property(ROOT_PATH);
        }

        @GwtIncompatible
        default SSLContext sslContext() {
            return (SSLContext) property(SSL_CONTEXT);
        }

        default SSLClientAuthentication sslClientAuthentication() {
            return (SSLClientAuthentication) property(SSL_CLIENT_AUTHENTICATION);
        }

        default UriBuilder baseUriBuilder() {
            return null;
        }

        @GwtIncompatible
        default URI baseUri() {
            return null;
        }

        static Builder builder() {
            return null;
        }

        interface Builder {

            Configuration build();

            Builder property(String name, Object value);

            default Builder protocol(String protocol) {
                return property(PROTOCOL, protocol);
            }

            default Builder host(String host) {
                return property(HOST, host);
            }

            default Builder port(Integer port) {
                return property(PORT, port);
            }

            default Builder rootPath(String rootPath) {
                return property(ROOT_PATH, rootPath);
            }

            @GwtIncompatible
            default Builder sslContext(SSLContext sslContext) {
                return property(SSL_CONTEXT, sslContext);
            }

            default Builder sslClientAuthentication(SSLClientAuthentication sslClientAuthentication) {
                return property(SSL_CLIENT_AUTHENTICATION, sslClientAuthentication);
            }

            <T> Builder from(BiFunction<String, Class<T>, Optional<T>> propertiesProvider);

            default Builder from(Object externalConfig) {
                return this;
            }
        }
    }

    interface Instance {
    }
}

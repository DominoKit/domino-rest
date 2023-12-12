/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.ws.rs.core;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents the the HTML form data request entity encoded using the {@code "application/x-www-form-urlencoded"}
 * content type.
 *
 * @author Marek Potociar
 * @since 2.0
 */
public class Form {
    private final MultivaluedMap<String, String> parameters;

    public Form() {
        this(new AbstractMultivaluedMap<String, String>(new LinkedHashMap<String, List<String>>()) {
            // by default, the items in a Form are iterable based on their insertion order.
        });
    }

    public Form(final String parameterName, final String parameterValue) {
        this();

        parameters.add(parameterName, parameterValue);
    }

    public Form(final MultivaluedMap<String, String> store) {
        this.parameters = store;
    }

    public Form param(final String name, final String value) {
        parameters.add(name, value);

        return this;
    }

    public MultivaluedMap<String, String> asMap() {
        return parameters;
    }
}

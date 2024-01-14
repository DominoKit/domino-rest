/*
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.GwtIncompatible;

public abstract class Link {

    public static final String TITLE = "title";

    public static final String REL = "rel";

    public static final String TYPE = "type";

    @GwtIncompatible
    public abstract URI getUri();

    public abstract UriBuilder getUriBuilder();

    public abstract String getRel();

    public abstract List<String> getRels();

    public abstract String getTitle();

    public abstract String getType();

    public abstract Map<String, String> getParams();

    @Override
    public abstract String toString();

    public interface Builder {

    }

    @Deprecated
    public static class JaxbLink {
    }

    @Deprecated
    public static class JaxbAdapter {
    }
}

/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;

public class EntityTag {
    private String value;
    private boolean weak;

    public EntityTag(final String value) {
        this(value, false);
    }

    public EntityTag(final String value, final boolean weak) {
        if (value == null) {
            throw new IllegalArgumentException("value==null");
        }
        this.value = value;
        this.weak = weak;
    }

    @Deprecated
    public static EntityTag valueOf(final String value) {
        return null;
    }

    public boolean isWeak() {
        return weak;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof EntityTag)) {
            return false;
        }

        EntityTag other = (EntityTag) obj;
        return Objects.equals(value, other.getValue()) && weak == other.isWeak();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value, this.weak);
    }

    @Override
    @Deprecated
    public String toString() {
        return null;
    }
}

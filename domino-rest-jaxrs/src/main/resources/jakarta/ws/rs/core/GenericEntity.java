/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Copyright (c) 2006 Google Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jakarta.ws.rs.core;

import jakarta.ws.rs.GwtIncompatible;
import java.lang.reflect.Type;
import java.util.Objects;

@GwtIncompatible
public class GenericEntity<T> {

    private final Class<?> rawType;
    private final Type type;
    private final T entity;

    protected GenericEntity(final T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity must not be null");
        }
        this.entity = entity;
        this.type = null;
        this.rawType = entity.getClass();
    }

    public GenericEntity(final T entity, final Type genericType) {
        if (entity == null || genericType == null) {
            throw new IllegalArgumentException("Arguments must not be null.");
        }
        this.entity = entity;
        this.rawType = entity.getClass();
        this.type = genericType;
    }

    public final Class<?> getRawType() {
        return rawType;
    }

    public final Type getType() {
        return type;
    }

    public final T getEntity() {
        return entity;
    }

    @Override
    public boolean equals(final Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, type);
    }

    @Override
    public String toString() {
        return "GenericEntity{" + entity.toString() + ", " + type.toString() + "}";
    }
}

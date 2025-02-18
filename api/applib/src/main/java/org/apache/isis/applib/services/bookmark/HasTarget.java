/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.applib.services.bookmark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Property;

/**
 * @since 2.0 {@index}
 */
public interface HasTarget {

    @Property(
            editing = Editing.DISABLED
    )
    @Parameter(
    )
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Target {
        int MAX_LENGTH = 2000;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }

    /**
     * A unique identifier (a GUID).
     */
    @Target
    Bookmark getTarget();


}

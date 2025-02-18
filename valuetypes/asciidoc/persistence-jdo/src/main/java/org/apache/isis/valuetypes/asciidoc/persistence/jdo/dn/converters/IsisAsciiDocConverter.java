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
package org.apache.isis.valuetypes.asciidoc.persistence.jdo.dn.converters;

import org.datanucleus.store.types.converters.TypeConverter;

import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

/**
 * @since 2.0 {@index}
 */
public class IsisAsciiDocConverter implements TypeConverter<AsciiDoc, String>{

    private static final long serialVersionUID = 1L;

    @Override
    public String toDatastoreType(final AsciiDoc memberValue) {
        return memberValue != null
                ? memberValue.getAdoc()
                : null;
    }

    @Override
    public AsciiDoc toMemberType(final String datastoreValue) {
        return datastoreValue != null
                ? AsciiDoc.valueOf(datastoreValue)
                : null;
    }

}

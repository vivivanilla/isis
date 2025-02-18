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
package org.apache.isis.persistence.jpa.integration.typeconverters.schema.v2;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.schema.ixn.v2.InteractionDto;

/**
 * @since 2.0 {@index}
 */
@Converter(autoApply = true)
public class IsisInteractionDtoConverter implements AttributeConverter<InteractionDto, String> {

    private static final long serialVersionUID = 1L;

    @Override
    public String convertToDatabaseColumn(final InteractionDto memberValue) {
        return memberValue != null
                ? InteractionDtoUtils.toXml(memberValue)
                        : null;
    }

    @Override
    public InteractionDto convertToEntityAttribute(final String datastoreValue) {
        return datastoreValue != null
                ? InteractionDtoUtils.fromXml(datastoreValue)
                        : null;
    }

}

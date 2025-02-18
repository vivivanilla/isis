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
package org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity;

import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.jdo.identity.ObjectIdentity;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.bookmark.idstringifiers.IdStringifierForUuid;

import lombok.Builder;
import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LATE)
@Builder
public class IdStringifierForObjectIdentity extends IdStringifier.Abstract<ObjectIdentity> {

    @Inject IdStringifierForUuid idStringifierForUuid;

    public IdStringifierForObjectIdentity() {
        super(ObjectIdentity.class);
    }

    /**
     * for testing only
     * @param idStringifierForUuid
     */
    @Builder
    IdStringifierForObjectIdentity(IdStringifierForUuid idStringifierForUuid) {
        this();
        this.idStringifierForUuid = idStringifierForUuid;
    }

    @Override
    public String enstring(@NonNull ObjectIdentity value) {
        Object keyAsObject = value.getKeyAsObject();
        if (keyAsObject instanceof UUID) {
            UUID uuid = (UUID) keyAsObject;
            return idStringifierForUuid.enstring(uuid);
        }
        // rely on JDO spec (5.4.3)
        return value.toString();
    }

    @Override
    public ObjectIdentity destring(@NonNull String stringified, @NonNull Class<?> targetEntityClass) {
        if (idStringifierForUuid.recognizes(stringified)) {
            UUID uuid = idStringifierForUuid.destring(stringified, targetEntityClass);
            return new ObjectIdentity(targetEntityClass, uuid);
        }
        return new ObjectIdentity(targetEntityClass, stringified);
    }
}

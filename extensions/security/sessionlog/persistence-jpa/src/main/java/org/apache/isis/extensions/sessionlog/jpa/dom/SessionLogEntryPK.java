/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.sessionlog.jpa.dom;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;
import org.apache.isis.extensions.sessionlog.applib.dom.SessionLogEntry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(of = {"sessionGuid"})
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SessionLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(name = SessionLogEntry.SessionGuid.NAME, nullable = SessionLogEntry.SessionGuid.NULLABLE, length = SessionLogEntry.SessionGuid.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID sessionGuid;

    @Override
    public String toString() {
        return sessionGuid != null ? sessionGuid.toString() : null;
    }


    @Component
    public static class Stringifier extends IdStringifier.AbstractWithPrefix<SessionLogEntryPK> {

        public Stringifier() {
            super(SessionLogEntryPK.class, "u");
        }

        @Override
        public String doEnstring(SessionLogEntryPK value) {
            return value.getSessionGuid().toString();
        }

        @Override
        protected SessionLogEntryPK doDestring(@NonNull String stringified, @NonNull Class<?> targetEntityClass) {
            return new SessionLogEntryPK(UUID.fromString(stringified));
        }
    }
}

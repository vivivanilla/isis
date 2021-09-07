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
package org.apache.isis.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.applib.services.bookmark.Bookmark;


/**
 * Allows domain objects that represents some sort of recorded change to a
 * domain object (commands, audit entries, published interactions) to act
 * as a mixee in order that other modules can contribute behaviour.
 *
 * @since 2.0 {@index}
 */
public interface DomainChangeRecord extends HasInteractionId, HasUsername {

    @Property(
            domainEvent = ChangeTypeMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ChangeTypeMeta.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Identifiers",
            sequence = "1",
            typicalLength = ChangeTypeMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ChangeTypeMeta.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Change Type",
            typicalLength = ChangeTypeMeta.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface ChangeTypeMeta {
        int MAX_LENGTH = 24;
        int TYPICAL_LENGTH = 24;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, DomainChangeRecord.ChangeType> {}
    }

    /**
     * Enumerates the different types of changes recognised.
     *
     * @since 2.0 {@index}
     */
    enum ChangeType {
        COMMAND,
        AUDIT_ENTRY,
        PUBLISHED_INTERACTION;
        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }

    /**
     * Distinguishes commands from audit entries from published events/interactions (when these are shown mixed together in a (standalone) table).
     */
    @ChangeTypeMeta
    ChangeType getType();



    @Property(
            domainEvent = InteractionId.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = InteractionId.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId = "Identifiers",
            sequence = "50",
            typicalLength = InteractionId.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = InteractionId.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = InteractionId.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface InteractionId {
        int MAX_LENGTH = 36;
        int TYPICAL_LENGTH = 36;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, UUID> { }
    }


    /**
     * The unique identifier (a GUID) of the
     * {@link org.apache.isis.applib.services.iactn.Interaction} within which
     * this change occurred.
     */
    @InteractionId
    @Override
    UUID getInteractionId();



    @Property(
            domainEvent = Username.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Username.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "10",
            typicalLength = Username.TYPICAL_LENGTH,
            hidden = Where.PARENTED_TABLES
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Username",
            typicalLength = Username.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Username {
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 40;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, String> {}
    }

    /**
     * The user that caused the change.
     */
    @Username
    String getUsername();



    @Property(
            domainEvent = DomainChangeRecord.Timestamp.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Timestamp.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "20",
            typicalLength = Timestamp.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = Timestamp.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Timestamp",
            typicalLength = Timestamp.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Timestamp {
        int MAX_LENGTH = 32;
        int TYPICAL_LENGTH = 32;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, java.sql.Timestamp> {}
    }

    /**
     * The time that the change occurred.
     */
    @DomainChangeRecord.Timestamp
    java.sql.Timestamp getTimestamp();


    /**
     * The object type of the domain object being changed.
     */
    @Property
    @PropertyLayout(
            named="Object Type",
            fieldSetId="Target",
            sequence = "10")
    default String getTargetObjectType() {
        return getTarget().getLogicalTypeName();
    }



    /**
     * The {@link Bookmark} identifying the domain object that has changed.
     */
    @Property
    @PropertyLayout(
            named="Object",
            fieldSetId="Target",
            sequence="30")
    Bookmark getTarget();


    /**
     * The member interaction (ie action invocation or property edit) which caused the domain object to be changed.
     *
     * <p>
     *     Populated for commands and for published events that represent action invocations or property edits.
     * </p>
     */
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(named="Member", hidden = Where.ALL_EXCEPT_STANDALONE_TABLES, fieldSetId="Target", sequence = "20")
    String getTargetMember();


    /**
     * The value of the property prior to it being changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(hidden = Where.ALL_EXCEPT_STANDALONE_TABLES, fieldSetId="Detail",sequence = "6")
    String getPreValue();


    /**
     * The value of the property after it has changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @Property(optionality = Optionality.MANDATORY)
    @PropertyLayout(hidden = Where.ALL_EXCEPT_STANDALONE_TABLES, fieldSetId="Detail",
    sequence = "7")
    String getPostValue();


}

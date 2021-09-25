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

    @Property(
            domainEvent = ChangeTypeMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ChangeTypeMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Identifiers",
            sequence = "1",
            typicalLength = ChangeTypeMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ChangeTypeMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
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
     * Distinguishes commands from audit entries from published events/interactions (when these are shown mixed together in a (standalone) table).
     */
    @ChangeTypeMeta
    ChangeType getType();



    @Property(
            domainEvent = InteractionId.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = InteractionId.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId = "Identifiers",
            sequence = "50",
            typicalLength = InteractionId.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = InteractionId.MAX_LENGTH,
            optionality = Optionality.MANDATORY
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
            maxLength = Username.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "10",
            typicalLength = Username.TYPICAL_LENGTH,
            hidden = Where.PARENTED_TABLES
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH,
            optionality = Optionality.MANDATORY
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
            domainEvent = TimestampMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = TimestampMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId="Identifiers",
            sequence = "20",
            typicalLength = TimestampMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = TimestampMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            named = "Timestamp",
            typicalLength = TimestampMeta.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface TimestampMeta {
        int MAX_LENGTH = 32;
        int TYPICAL_LENGTH = 32;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, java.sql.Timestamp> {}
    }

    /**
     * The time that the change occurred.
     */
    @DomainChangeRecord.TimestampMeta
    java.sql.Timestamp getTimestamp();



    @Property(
            domainEvent = LogicalTypeName.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = LogicalTypeName.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            named="Logical Type Name",
            fieldSetId="Target",
            sequence = "10",
            typicalLength = LogicalTypeName.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = LogicalTypeName.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            typicalLength = LogicalTypeName.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface LogicalTypeName {
        int MAX_LENGTH = 1024;
        int TYPICAL_LENGTH = 128;

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, Bookmark> { }
    }

    /**
     * The logical type of the domain object being changed.
     */
    @LogicalTypeName
    default String getTargetLogicalTypeName() {
        return getTarget().getLogicalTypeName();
    }



    @Property(
            domainEvent = TargetMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = TargetMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            named = "Object",
            fieldSetId = "Target",
            sequence = "30"
    )
    @Parameter(
            maxLength = TargetMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            named = "Object"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface TargetMeta {
        int MAX_LENGTH = 2000; // serialized bookmark

        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, Bookmark> { }
    }

    /**
     * The {@link Bookmark} identifying the domain object that has changed.
     */
    @DomainChangeRecord.TargetMeta
    Bookmark getTarget();



    @Property(
            domainEvent = TargetMember.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = TargetMember.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId="Target",
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            named = "Member",
            sequence = "20",
            typicalLength = TargetMember.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = TargetMember.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named="Member",
            typicalLength = TargetMember.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface TargetMember {
        int MAX_LENGTH = 255;
        int TYPICAL_LENGTH = 30;
        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, String> { }
    }

    /**
     * The member interaction (ie action invocation or property edit) which caused the domain object to be changed.
     *
     * <p>
     *     Populated for commands and for published events that represent action invocations or property edits.
     * </p>
     */
    @DomainChangeRecord.TargetMember
    String getTargetMember();


    @Property(
            domainEvent = PreValue.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Detail",
            sequence = "6",
            typicalLength = PreValue.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = PreValue.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            typicalLength = PreValue.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface PreValue {
        int MAX_LENGTH = 1024;
        int TYPICAL_LENGTH = 30;
        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, String> { }
    }

    /**
     * The value of the property prior to it being changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @PreValue
    String getPreValue();



    @Property(
            domainEvent = PostValue.DomainEvent.class,
            editing = Editing.DISABLED,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_EXCEPT_STANDALONE_TABLES,
            fieldSetId = "Detail",
            sequence = "7",
            typicalLength = PostValue.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = PostValue.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            typicalLength = PostValue.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface PostValue {
        int MAX_LENGTH = 1024;
        int TYPICAL_LENGTH = 30;
        class DomainEvent extends PropertyDomainEvent<DomainChangeRecord, String> { }
    }

    /**
     * The value of the property after it has changed.
     *
     * <p>
     * Populated only for audit entries.
     * </p>
     */
    @PostValue
    String getPostValue();


}

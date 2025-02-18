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

package org.apache.isis.extensions.audittrail.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import javax.inject.Named;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.applib.mixins.system.HasInteractionIdAndSequence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._Times;
import org.apache.isis.extensions.audittrail.applib.IsisModuleExtAuditTrailApplib;

import lombok.val;
import lombok.experimental.UtilityClass;

@Named(AuditTrailEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = AuditTrailEntry.TitleUiEvent.class,
        iconUiEvent = AuditTrailEntry.IconUiEvent.class,
        cssClassUiEvent = AuditTrailEntry.CssClassUiEvent.class,
        layoutUiEvent = AuditTrailEntry.LayoutUiEvent.class
)
public abstract class AuditTrailEntry implements DomainChangeRecord, Comparable<AuditTrailEntry> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtAuditTrailApplib.NAMESPACE + ".AuditTrailEntry";
    public static final String SCHEMA = IsisModuleExtAuditTrailApplib.SCHEMA;
    public static final String TABLE = "AuditTrailEntry";

    @UtilityClass
    public static class Nq {
        public static final String FIND_FIRST_BY_TARGET = "findFirstByTarget";
        public static final String FIND_RECENT_BY_TARGET = "findRecentByTarget";
        public static final String FIND_RECENT_BY_TARGET_AND_PROPERTY_ID = "findRecentByTargetAndPropertyId";
        public static final String FIND_BY_INTERACTION_ID = "findByInteractionId";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN = "findByTargetAndTimestampBetween";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_AFTER = "findByTargetAndTimestampAfter";
        public static final String FIND_BY_TARGET_AND_TIMESTAMP_BEFORE = "findByTargetAndTimestampBefore";
        public static final String FIND_BY_TARGET = "findByTarget";
        public static final String FIND_BY_TIMESTAMP_BETWEEN = "findByTimestampBetween";
        public static final String FIND_BY_TIMESTAMP_AFTER = "findByTimestampAfter";
        public static final String FIND_BY_TIMESTAMP_BEFORE = "findByTimestampBefore";
        public static final String FIND = "find";
    }

    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends IsisModuleExtAuditTrailApplib.TitleUiEvent<AuditTrailEntry> { }
    public static class IconUiEvent extends IsisModuleExtAuditTrailApplib.IconUiEvent<AuditTrailEntry> { }
    public static class CssClassUiEvent extends IsisModuleExtAuditTrailApplib.CssClassUiEvent<AuditTrailEntry> { }
    public static class LayoutUiEvent extends IsisModuleExtAuditTrailApplib.LayoutUiEvent<AuditTrailEntry> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtAuditTrailApplib.PropertyDomainEvent<AuditTrailEntry, T> { }
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtAuditTrailApplib.CollectionDomainEvent<AuditTrailEntry, T> { }
    public static abstract class ActionDomainEvent extends IsisModuleExtAuditTrailApplib.ActionDomainEvent<AuditTrailEntry> { }


    @Programmatic
    public void init(final EntityPropertyChange change) {
        setTimestamp(change.getTimestamp());
        setUsername(change.getUsername());
        setTarget(change.getTarget());
        setLogicalMemberIdentifier(change.getLogicalMemberIdentifier());
        setSequence(change.getSequence());
        setPropertyId(change.getPropertyId());
        setPreValue(_Strings.trimmed(change.getPreValue(), PreValue.MAX_LENGTH));
        setPostValue(_Strings.trimmed(change.getPostValue(), PostValue.MAX_LENGTH));
        setInteractionId(change.getInteractionId());
    }

    public String title() {
        val buf = new TitleBuffer();
        buf.append(_Times.DEFAULT_LOCAL_DATETIME_FORMATTER
                .format(getTimestamp().toLocalDateTime()));
        buf.append(" ").append(getLogicalMemberIdentifier());
        return buf.toString();
    }


    @DomainChangeRecord.Type
    @Override
    public ChangeType getType() {
        return ChangeType.AUDIT_ENTRY;
    }


    @Property(
            domainEvent = InteractionId.DomainEvent.class
    )
    @DomainChangeRecord.InteractionId
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InteractionId {
        class DomainEvent extends PropertyDomainEvent<UUID> {}
        String NAME = "interactionId";
        int MAX_LENGTH = HasInteractionId.InteractionId.MAX_LENGTH;
        boolean NULLABLE = HasInteractionId.InteractionId.NULLABLE;
        String ALLOWS_NULL = HasInteractionId.InteractionId.ALLOWS_NULL;
    }
    @Override
    @InteractionId
    public abstract UUID getInteractionId();
    public abstract void setInteractionId(UUID interactionId);


    @Property(
            domainEvent = Username.DomainEvent.class
    )
    @DomainChangeRecord.Username
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Username {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.Username.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Username.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Username.ALLOWS_NULL;
    }
    @Override
    @Username
    public abstract String getUsername();
    public abstract void setUsername(String userName);


    @Property(
            domainEvent = Timestamp.DomainEvent.class
    )
    @DomainChangeRecord.Timestamp
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Timestamp {
        class DomainEvent extends PropertyDomainEvent<java.sql.Timestamp> {}
        boolean NULLABLE = DomainChangeRecord.Timestamp.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Timestamp.ALLOWS_NULL;
    }
    @Timestamp
    @Override
    public abstract java.sql.Timestamp getTimestamp();
    public abstract void setTimestamp(java.sql.Timestamp timestamp);



    @Property(
            domainEvent = Target.DomainEvent.class
    )
    @DomainChangeRecord.Target
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Target {
        class DomainEvent extends PropertyDomainEvent<Bookmark> {}
        int MAX_LENGTH = DomainChangeRecord.Target.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.Target.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.Target.ALLOWS_NULL;
    }
    @Override
    @Target
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);



    /**
     * The 0-based additional identifier of an execution event within the given {@link #getInteractionId() interaction}.
     *
     * <p>
     * The combination of ({@link #getInteractionId() interactionId}, {@link #getSequence() sequence}) makes up the
     * primary key.
     * </p>
     */
    @Property(
            domainEvent = Sequence.DomainEvent.class
    )
    @HasInteractionIdAndSequence.Sequence
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Sequence {
        class DomainEvent extends PropertyDomainEvent<Integer> {}
        String NAME = "sequence";
        boolean NULLABLE = HasInteractionIdAndSequence.Sequence.NULLABLE;
        String ALLOWS_NULL = HasInteractionIdAndSequence.Sequence.ALLOWS_NULL;
    }
    @Sequence
    public abstract int getSequence();
    public abstract void setSequence(int sequence);



    @Property(
            domainEvent = LogicalMemberIdentifier.DomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @DomainChangeRecord.LogicalMemberIdentifier
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogicalMemberIdentifier {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.LogicalMemberIdentifier.MAX_LENGTH;
        boolean NULLABLE = DomainChangeRecord.LogicalMemberIdentifier.NULLABLE;
        String ALLOWS_NULL = DomainChangeRecord.LogicalMemberIdentifier.ALLOWS_NULL;

    }
    @Override
    @LogicalMemberIdentifier
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);


    @Property(
            domainEvent = PropertyId.DomainEvent.class,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @DomainChangeRecord.PropertyId
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PropertyId {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.PropertyId.MAX_LENGTH;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";

    }
    @Override
    @PropertyId
    public abstract String getPropertyId();
    public abstract void setPropertyId(String propertyId);



    @Property(
            domainEvent = PreValue.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @DomainChangeRecord.PreValue
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PreValue {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.PreValue.MAX_LENGTH;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";

    }
    @Override
    @PreValue
    public abstract String getPreValue();
    public abstract void setPreValue(String preValue);


    @Property(
            domainEvent = PostValue.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @DomainChangeRecord.PostValue
    @java.lang.annotation.Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PostValue {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = DomainChangeRecord.PostValue.MAX_LENGTH;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";

    }
    @Override
    @PostValue
    public abstract String getPostValue();
    public abstract void setPostValue(String postValue);


    private static final ObjectContracts.ObjectContract<AuditTrailEntry> contract	=
            ObjectContracts.contract(AuditTrailEntry.class)
                    .thenUse("timestamp", AuditTrailEntry::getTimestamp)
                    .thenUse("username", AuditTrailEntry::getUsername)
                    .thenUse("target", e -> e.getTarget() != null ? e.getTarget().toString(): null)
                    .thenUse("propertyId", AuditTrailEntry::getPropertyId)
            ;


    @Override
    public String toString() {
        return contract.toString(AuditTrailEntry.this);
    }

    @Override
    public int compareTo(final AuditTrailEntry other) {
        return contract.compare(this, other);
    }

}

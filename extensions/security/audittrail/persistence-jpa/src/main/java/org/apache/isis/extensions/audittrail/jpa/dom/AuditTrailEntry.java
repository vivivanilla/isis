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

package org.apache.isis.extensions.audittrail.jpa.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntry.Nq;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.persistence.jpa.integration.typeconverters.applib.IsisBookmarkConverter;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;

import lombok.Getter;
import lombok.Setter;
@Entity
@Table(
        schema = AuditTrailEntry.SCHEMA,
        name = AuditTrailEntry.TABLE,
        indexes = {
                @Index(name = "target_propertyId_timestamp_IDX", columnList = "target, propertyId, timestamp", unique = false),
                @Index(name = "target_timestamp_IDX", columnList = "target, timestamp", unique = false),
                @Index(name = "timestamp_IDX", columnList = "timestamp", unique = false),
                @Index(name = "interactionId_IDX", columnList = "interactionId", unique = false)
        }
)
@NamedQueries( {
    @NamedQuery(
            name = Nq.FIND_BY_INTERACTION_ID,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.interactionId = :interactionId"),
    @NamedQuery(
            name = Nq.FIND_FIRST_BY_TARGET,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + " ORDER BY e.timestamp ASC "),  // programmatic range 0,2
    @NamedQuery(
            name = Nq.FIND_RECENT_BY_TARGET,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + " ORDER BY e.timestamp DESC "),   // programmatic range 0,100
    @NamedQuery(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + "   AND e.timestamp >= :from "
                  + "   AND e.timestamp <= :to "
                  + "ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name = Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + "   AND e.timestamp >= :from "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + "   AND e.timestamp <= :to "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TARGET,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_BETWEEN,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.timestamp >= :from "
                  + "   AND e.timestamp <= :to "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_AFTER,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.timestamp >= :from "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND_BY_TIMESTAMP_BEFORE,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.timestamp <= :to "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name  = Nq.FIND,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " ORDER BY e.timestamp DESC"),
    @NamedQuery(
            name = Nq.FIND_RECENT_BY_TARGET_AND_PROPERTY_ID,
            query = "SELECT e "
                  + "  FROM AuditTrailEntry e "
                  + " WHERE e.target = :target "
                  + "   AND e.propertyId = :propertyId "
                  + " ORDER BY e.timestamp DESC ") // programmatic limit 0,30
})
@EntityListeners(IsisEntityListener.class)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
@Named(AuditTrailEntry.LOGICAL_TYPE_NAME)
@DomainObject(
        editing = Editing.DISABLED,
        entityChangePublishing = Publishing.DISABLED
)
public class AuditTrailEntry
extends org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntry {

    private Long id;

    @Id
    @Programmatic
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    private Long version;

    @Version
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }



    @Getter @Setter
    private String username;

    @Column(nullable = Username.NULLABLE, length = Username.MAX_LENGTH)
    @Username
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    @Getter @Setter
    private java.sql.Timestamp timestamp;

    @Column(nullable = Timestamp.NULLABLE)
    @Timestamp
    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    @Getter @Setter
    private UUID interactionId;

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(nullable = InteractionId.NULLABLE, length = InteractionId.MAX_LENGTH)
    @InteractionId
    public UUID getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(UUID interactionId) {
        this.interactionId = interactionId;
    }


    @Getter @Setter
    private int sequence;

    @Column(nullable = Sequence.NULLABLE)
    @Sequence
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }



    @Getter @Setter
    private Bookmark target;

    @Convert(converter = IsisBookmarkConverter.class)
    @Column(nullable = Target.NULLABLE, length = Target.MAX_LENGTH)
    @Target
    public Bookmark getTarget() {
        return target;
    }

    public void setTarget(Bookmark target) {
        this.target = target;
    }


    @Getter @Setter
    private String logicalMemberIdentifier;

    @Column(nullable = LogicalMemberIdentifier.NULLABLE, length = LogicalMemberIdentifier.MAX_LENGTH)
    @LogicalMemberIdentifier
    public String getLogicalMemberIdentifier() {
        return logicalMemberIdentifier;
    }

    public void setLogicalMemberIdentifier(String logicalMemberIdentifier) {
        this.logicalMemberIdentifier = logicalMemberIdentifier;
    }


    @Getter @Setter
    private String propertyId;

    @Column(nullable = PropertyId.NULLABLE, length = PropertyId.MAX_LENGTH)
    @PropertyId
    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }


    @Getter @Setter
    private String preValue;

    @Column(nullable = PreValue.NULLABLE, length = PreValue.MAX_LENGTH)
    @PreValue
    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(String preValue) {
        this.preValue = preValue;
    }


    @Getter @Setter
    private String postValue;


    @Column(nullable = PostValue.NULLABLE, length = PostValue.MAX_LENGTH)
    @PostValue
    public String getPostValue() {
        return postValue;
    }

    public void setPostValue(String postValue) {
        this.postValue = postValue;
    }
}

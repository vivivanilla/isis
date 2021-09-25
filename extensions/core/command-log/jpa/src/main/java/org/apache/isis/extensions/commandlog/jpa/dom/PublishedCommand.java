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
package org.apache.isis.extensions.commandlog.jpa.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.persistence.jpa.integration.typeconverters.v2.IsisCommandDtoConverter;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.NoArgsConstructor;

@Entity
@Table(
        schema = "isisExtensionsCommandLog",
        name = "PublishedCommand",
        uniqueConstraints =
        @UniqueConstraint(
                name = "PublishedCommand__interactionId__UNQ",
                columnNames = {
                        "interactionId"
                }),
        indexes = {
                @Index(
                        name = "PublishedCommand__startedAt__timestamp__IDX",
                        columnList = "startedAt, timestamp"
                ),
                @Index(
                        name = "PublishedCommand__timestamp__IDX",
                        columnList = "timestamp"
                )
        }
)
@NamedQueries({
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_INTERACTION_ID,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.interactionId = :interactionId"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_PARENT,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.parent = :parent "),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_CURRENT,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.completedAt is null "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_COMPLETED,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.completedAt is not null "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_TARGET,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.target = :target "
                    + "ORDER BY pc.timestamp DESC "),
                    // TODO: query.setMaxResults(30)
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BETWEEEN,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.target = :target "
                    + "   AND pc.timestamp >= :from "
                    + "   AND pc.timestamp <= :to "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.target = :target "
                    + "   AND pc.timestamp >= :from "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.target = :target "
                    + "   AND pc.timestamp <= :to "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.target = :target "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BETWEEN,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.timestamp >= :from "
                    + "   AND pc.timestamp <= :to "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_AFTER,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.timestamp >= :from "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BEFORE,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.timestamp <= :to "
                    + "ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " ORDER BY pc.timestamp DESC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_USERNAME,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.username = :username "
                    + "ORDER BY pc.timestamp DESC "),
                    // TODO: query.setMaxResults(30)
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_FIRST,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.startedAt   is not null "
                    + "   and pc.completedAt is not null "
                    + "ORDER BY pc.timestamp ASC "),
                    // TODO: query.setMaxResults(2)
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_SINCE,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.timestamp > :timestamp "
                    + "   AND pc.startedAt   is not null "
                    + "   AND pc.completedAt is not null "
                    + "ORDER BY pc.timestamp ASC"),
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_REPLAYED,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.replayState = 'OK' OR pc.replayState = 'FAILED' "
                    + "ORDER BY pc.timestamp DESC "),
                    // TODO: query.setMaxResults(2)
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_COMPLETED,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.startedAt   is not null "
                    + "   AND pc.completedAt is not null "
                    + "ORDER BY pc.timestamp DESC "),
                    // TODO: query.setMaxResults(2)
    @NamedQuery(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_NOT_YET_REPLAYED,
            query = "SELECT pc "
                    + "FROM PublishedCommand pc"
                    + " WHERE pc.replayState = 'PENDING' "
                    + "ORDER BY pc.timestamp ASC "),
                    // TODO: query.setMaxResults(2) // same as batch size
})
@EntityListeners(IsisEntityListener.class)
@DomainObject(
        logicalTypeName = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
@NoArgsConstructor
public class PublishedCommand extends org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand {

    @Version
    private Long version;

    // EVENTS
    // (see superclass)

    // CONSTRUCTORS

    public PublishedCommand(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {

        super(commandDto, replayState, targetIndex);
    }


    // TITLE
    // (see superclass)


    // (CHANGE) TYPE
    // (derived - see superclass)


    // INTERACTION ID

    @Id
    @Column(nullable = false, length = DomainChangeRecord.InteractionId.MAX_LENGTH)
    private UUID interactionId;

    @DomainChangeRecord.InteractionId
    @Override
    public UUID getInteractionId() {
        return interactionId;
    }
    @Override
    public void setInteractionId(UUID interactionId) {
        this.interactionId = interactionId;
    }


    // USER NAME

    @Column(nullable = false, length = DomainChangeRecord.Username.MAX_LENGTH)
    private String username;

    @DomainChangeRecord.Username
    @Override
    public String getUsername() { return username; }
    @Override
    public void setUsername(String username) { this.username = username; }


    // TIMESTAMP

    @Basic
    @Column(nullable = false, length = DomainChangeRecord.TimestampMeta.MAX_LENGTH)
    private Timestamp timestamp;

    @DomainChangeRecord.TimestampMeta
    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }
    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }



    // REPLAY STATE

    @Column(nullable = false, length = ReplayStateMeta.MAX_LENGTH)
    @Enumerated(EnumType.STRING)
    private ReplayState replayState;

    @ReplayStateMeta
    @Override
    public ReplayState getReplayState() {
        return replayState;
    }
    @Override
    public void setReplayState(ReplayState replayState) {
        this.replayState = replayState;
    }


    // REPLAY STATE FAILURE REASON

    @Column(nullable = true, length = ReplayStateFailureReason.MAX_LENGTH)
    private String replayStateFailureReason;

    @ReplayStateFailureReason
    @Override
    public String getReplayStateFailureReason() {
        return replayStateFailureReason;
    }
    @Override
    public void setReplayStateFailureReason(String replayStateFailureReason) {
        this.replayStateFailureReason = replayStateFailureReason;
    }


    // PARENT

    @ManyToOne
    @JoinColumn(name = "parentId", nullable = true)
    private PublishedCommand parent;

    @Parent
    @Override
    public PublishedCommand getParent() {
        return parent;
    }
    @Override
    public void setParent(org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand parent) {
        this.parent = _Casts.uncheckedCast(parent);
    }



    // TARGET

    /**
     * Optional in case the serialized bookmark exceeds length (if a view model)
     */
    @Basic
    @Column(nullable = true, length = DomainChangeRecord.TargetMeta.MAX_LENGTH)
    private Bookmark target;

    @DomainChangeRecord.TargetMeta
    @Override
    public Bookmark getTarget() {
        return target;
    }
    @Override
    public void setTarget(Bookmark target) {
        this.target = target;
    }


    // TARGET MEMBER
    // (derived - see superclass)



    // LOCAL MEMBER
    // (derived - see superclass)



    // LOGICAL MEMBER IDENTIFIER

    @Column(nullable = false, length = MemberIdentifierType.Meta.MAX_LENGTH)
    private String logicalMemberIdentifier;

    @LogicalMemberIdentifier
    @Override
    public String getLogicalMemberIdentifier() {
        return logicalMemberIdentifier;
    }
    @Override
    public void setLogicalMemberIdentifier(String logicalMemberIdentifier) {
        this.logicalMemberIdentifier = logicalMemberIdentifier;
    }


    // COMMAND DTO

    @Lob
    @Convert(converter = IsisCommandDtoConverter.class) // hmm, shouldn't be necessary as this is converter has autoApply=true
    @Column(nullable = true)
    private CommandDto commandDto;

    @CommandDtoMeta
    @Override
    public CommandDto getCommandDto() {
        return commandDto;
    }
    @Override
    public void setCommandDto(CommandDto commandDto) {
        this.commandDto = commandDto;
    }


    // STARTED AT

    @Basic
    @Column(nullable = true, length = StartedAt.MAX_LENGTH)
    private Timestamp startedAt;

    @StartedAt
    @Override
    public Timestamp getStartedAt() {
        return startedAt;
    }
    @Override
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }


    // COMPLETED AT

    @Basic
    @Column(nullable = true, length = CompletedAt.MAX_LENGTH)
    private Timestamp completedAt;

    @CompletedAt
    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }
    @Override
    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }



    // DURATION
    // (derived - see superclass)



    // COMPLETE
    // (derived - see superclass)



    // RESULT SUMMARY
    // (derived - see superclass)



    // RESULT

    @Basic
    @Column(nullable = true, length = Result.MAX_LENGTH)
    private Bookmark result;

    @Result
    @Override
    public Bookmark getResult() {
        return result;
    }
    @Override
    public void setResult(Bookmark result) {
        this.result = result;
    }


    // EXCEPTION

    @Lob
    @Column(nullable = true)
    private String exception;

    @ExceptionMeta
    @Override
    public String getException() {
        return exception;
    }
    @Override
    public void setException(final String exception) {
        this.exception = exception;
    }


    // CAUSED EXCEPTION
    // (derived - see superclass)



    // PRE VALUE
    // (derived - see superclass)



    // POST VALUE
    // (derived - see superclass)



    // PROGRAMMATIC
    // (see superclass)


    // TO STRING etc
    // (see superclass)


}


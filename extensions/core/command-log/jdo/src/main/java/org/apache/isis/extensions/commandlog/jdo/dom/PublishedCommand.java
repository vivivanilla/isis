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
package org.apache.isis.extensions.commandlog.jdo.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = "isisExtensionsCommandLog",
        table = "PublishedCommand"
)
@Uniques({
        @Unique(
                name = "PublishedCommand__interactionId__UNQ",
                members = {
                        "interactionId"
                })
})
@Indices({
        @Index(
                name = "PublishedCommand__startedAt__timestamp__IDX",
                members = {"startedAt", "timestamp"}),
        @Index(
                name = "PublishedCommand__timestamp__IDX",
                members = {"timestamp"}
        ),
})
@Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
// queries that use RANGE 0,2 should instead be RANGE 0,1, however this results in DataNucleus submitting "FETCH NEXT ROW ONLY"
// which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
@Queries( {
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_INTERACTION_ID,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE interactionId == :interactionId "),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_PARENT,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE parent == :parent "),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_CURRENT,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE completedAt == null "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_COMPLETED,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE completedAt != null "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_TARGET,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BETWEEEN,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TARGET,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BETWEEN,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_AFTER,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BEFORE,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " ORDER BY this.timestamp DESC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_USERNAME,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE username == :username "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_FIRST,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE startedAt  != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,2"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_SINCE,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE timestamp > :timestamp "
                    + "   && startedAt != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_REPLAYED,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE (replayState == 'OK' || replayState == 'FAILED') "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_COMPLETED,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
    @Query(
            name = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.NAMED_QUERY_FIND_NOT_YET_REPLAYED,
            value = "SELECT "
                    + "FROM " + PublishedCommand.FQCN
                    + " WHERE replayState == 'PENDING' "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,10"),    // same as batch size
})
@DomainObject(
        logicalTypeName = org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PublishedCommand extends org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand {

    protected final static String FQCN = "org.apache.isis.extensions.commandlog.jdo.dom.PublishedCommand";

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

    @PrimaryKey
    @Persistent
    @Column(allowsNull = "false", length = DomainChangeRecord.InteractionId.MAX_LENGTH)
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

    @Column(allowsNull = "false", length = DomainChangeRecord.Username.MAX_LENGTH)
    private String username;

    @DomainChangeRecord.Username
    @Override
    public String getUsername() { return username; }
    @Override
    public void setUsername(String username) { this.username = username; }


    // TIMESTAMP

    @Persistent
    @Column(allowsNull = "false", length = DomainChangeRecord.TimestampMeta.MAX_LENGTH)
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

    @Column(allowsNull="true", length = ReplayStateMeta.MAX_LENGTH)
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

    @Column(allowsNull = "true", length = ReplayStateFailureReason.MAX_LENGTH)
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

    @Persistent
    @Column(name = "parentId", allowsNull = "true")
    private PublishedCommand parent;

    @Parent
    @Override
    public org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand getParent() {
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
    @Persistent
    @Column(allowsNull = "true", length = DomainChangeRecord.TargetMeta.MAX_LENGTH)
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

    @Column(allowsNull = "false", length = MemberIdentifierType.Meta.MAX_LENGTH)
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

    @Persistent
    @Column(allowsNull = "true", jdbcType="CLOB")
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

    @Persistent
    @Column(allowsNull = "true", length = StartedAt.MAX_LENGTH)
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

    @Persistent
    @Column(allowsNull = "true", length = CompletedAt.MAX_LENGTH)
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

    @Persistent
    @Column(allowsNull = "true", length = Result.MAX_LENGTH)
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

    @Column(allowsNull = "true", jdbcType = "CLOB")
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


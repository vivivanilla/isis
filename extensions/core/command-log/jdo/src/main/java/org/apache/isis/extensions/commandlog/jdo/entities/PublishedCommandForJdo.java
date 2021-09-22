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
package org.apache.isis.extensions.commandlog.jdo.entities;

import java.sql.Timestamp;
import java.util.UUID;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.types.MemberIdentifierType;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = "isisExtensionsCommandLog",
        table = "PublishedCommand")

// queries that use RANGE 0,2 should instead be RANGE 0,1, however this results in DataNucleus submitting "FETCH NEXT ROW ONLY"
// which SQL Server doesn't understand.  However, as workaround, SQL Server *does* understand FETCH NEXT 2 ROWS ONLY
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_INTERACTION_ID,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE interactionId == :interactionId "),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_PARENT,
            value="SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE parent == :parent "),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_CURRENT,
            value="SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE completedAt == null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_COMPLETED,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE completedAt != null "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_TARGET,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BETWEEEN,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_AFTER,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BEFORE,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE target == :target "
                    + "&& timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TARGET,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE target == :target "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BETWEEN,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE timestamp >= :from "
                    + "&&    timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_AFTER,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE timestamp >= :from "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BEFORE,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE timestamp <= :to "
                    + "ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " ORDER BY this.timestamp DESC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_USERNAME,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE username == :username "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,30"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_FIRST,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,2"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_SINCE,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE timestamp > :timestamp "
                    + "   && startedAt != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp ASC"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_REPLAYED,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE (replayState == 'OK' || replayState == 'FAILED') "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_COMPLETED,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE startedAt   != null "
                    + "   && completedAt != null "
                    + "ORDER BY this.timestamp DESC "
                    + "RANGE 0,2"),
    @javax.jdo.annotations.Query(
            name = PublishedCommand.NAMED_QUERY_FIND_NOT_YET_REPLAYED,
            value = "SELECT "
                    + "FROM " + PublishedCommandForJdo.FQCN
                    + " WHERE replayState == 'PENDING' "
                    + "ORDER BY this.timestamp ASC "
                    + "RANGE 0,10"),    // same as batch size
})
@javax.jdo.annotations.Indices({
        @javax.jdo.annotations.Index(name = "CommandJdo__startedAt__timestamp__IDX", members = { "startedAt", "timestamp" }),
        @javax.jdo.annotations.Index(name = "CommandJdo__timestamp__IDX", members = { "timestamp" }),
})
@DomainObject(
        logicalTypeName = PublishedCommand.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        named = "Published Command"
)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PublishedCommandForJdo extends PublishedCommand {

    protected final static String FQCN = "org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo";

    // EVENTS
    // (see superclass)

    // CONSTRUCTORS

    public PublishedCommandForJdo(
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

    @javax.jdo.annotations.PrimaryKey
    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false", name = "interactionId", length = 36)
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

    @javax.jdo.annotations.Column(allowsNull="false", length = 50)
    private String username;

    @DomainChangeRecord.Username
    @Override
    public String getUsername() { return username; }
    @Override
    public void setUsername(String username) { this.username = username; }


    // TIMESTAMP

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="false")
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

    @javax.jdo.annotations.Column(allowsNull="true", length=10)
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

    @javax.jdo.annotations.Column(allowsNull="true", length=255)
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

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(name="parentId", allowsNull="true")
    private PublishedCommandForJdo parent;

    @Parent
    @Override
    public PublishedCommand getParent() {
        return parent;
    }
    @Override
    public void setParent(PublishedCommand parent) {
        this.parent = _Casts.uncheckedCast(parent);
    }



    // TARGET

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="target")
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

    @javax.jdo.annotations.Column(allowsNull="false", length = MemberIdentifierType.Meta.MAX_LEN)
    @Getter @Setter
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

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
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

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
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

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true")
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

    @javax.jdo.annotations.Persistent
    @javax.jdo.annotations.Column(allowsNull="true", length = 2000, name="result")
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

    @javax.jdo.annotations.Column(allowsNull="true", jdbcType="CLOB")
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


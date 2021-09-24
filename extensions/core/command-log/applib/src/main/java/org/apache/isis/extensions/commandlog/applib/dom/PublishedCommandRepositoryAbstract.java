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
package org.apache.isis.extensions.commandlog.applib.dom;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.val;

/**
 * Provides supporting functionality for querying and persisting
 * {@link PublishedCommand command} entities.
 */
@Repository
@Named("isis.ext.commandLog.PublishedCommandRepository")
public abstract class PublishedCommandRepositoryAbstract<PC extends PublishedCommand>
implements PublishedCommandRepository {

    private final Class<PC> publishedCommandClass;

    @Inject Provider<RepositoryService> repositoryServiceProvider;

    protected PublishedCommandRepositoryAbstract(final Class<PC> publishedCommandClass) {
        this.publishedCommandClass = publishedCommandClass;
    }

    @Override
    public List<PublishedCommand> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<PC> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND);
            }
        }
        return _Casts.uncheckedCast(
                repositoryService().allMatches(query)
        );
    }

    @Override
    public Optional<PublishedCommand> findByInteractionId(final UUID interactionId) {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                Query.named(publishedCommandClass,
                            PublishedCommand.NAMED_QUERY_FIND_BY_INTERACTION_ID)
                    .withParameter("interactionId", interactionId.toString()))
        );
    }


    @Override
    public List<PublishedCommand> findByParent(final PublishedCommand parent) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass,
                            PublishedCommand.NAMED_QUERY_FIND_BY_PARENT)
                    .withParameter("parent", parent))
        );
    }

    @Override
    public List<PublishedCommand> findCurrent() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_CURRENT))
        );
    }

    @Override
    public List<PublishedCommand> findCompleted() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_COMPLETED))
        );
    }

    @Override
    public List<PublishedCommand> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<PC> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(publishedCommandClass,
                                PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BETWEEEN)
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(publishedCommandClass,
                                    PublishedCommand.NAMED_QUERY_FIND_BY_TARGET)
                        .withParameter("target", target);
            }
        }
        return _Casts.uncheckedCast(
                repositoryService().allMatches(query));
    }

    private static Timestamp toTimestampStartOfDayWithOffset(
            final @Nullable LocalDate dt,
            final int daysOffset) {

        return dt!=null
                ? new Timestamp(
                        Instant.from(dt.atStartOfDay().plusDays(daysOffset).atZone(ZoneId.systemDefault()))
                        .toEpochMilli())
                : null;
    }

    @Override
    public List<PublishedCommand> findRecentByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass,
                            PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_USERNAME)
                    .withParameter("username", username))
        );
    }

    @Override
    public List<PublishedCommand> findRecentByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass,
                            PublishedCommand.NAMED_QUERY_FIND_RECENT_BY_TARGET)
                    .withParameter("target", target))
        );
    }

    @Override
    public List<PublishedCommand> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        return findByInteractionId(interactionId)
                .map(from -> findSince(from.getTimestamp(), batchSize))
                .orElse(Collections.emptyList());
    }

    private List<PublishedCommand> findFirst() {
        Optional<PC> firstCommandIfAny = repositoryService().firstMatch(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_FIRST));
        return _Casts.uncheckedCast(
                    firstCommandIfAny
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList())
        );
    }


    private List<PublishedCommand> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        val needsTrimFix = batchSize != null && batchSize == 1;

        NamedQuery<PC> q = Query.named(publishedCommandClass,
                                       PublishedCommand.NAMED_QUERY_FIND_SINCE)
                                .withParameter("timestamp", timestamp);
        if(batchSize != null) {
            q = q.withRange(QueryRange.limit(
                    needsTrimFix ? 2L : batchSize
            ));
        }

        final List<PC> publishedCommands = repositoryService().allMatches(q);
        return _Casts.uncheckedCast(
                    needsTrimFix && publishedCommands.size() > 1
                        ? publishedCommands.subList(0,1)
                        : publishedCommands
                );
    }


    @Override
    public Optional<PublishedCommand> findMostRecentReplayed() {

        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_REPLAYED))
        );
    }

    @Override
    public Optional<PublishedCommand> findMostRecentCompleted() {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_MOST_RECENT_COMPLETED))
        );
    }

    @Override
    public List<PublishedCommand> findNotYetReplayed() {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_NOT_YET_REPLAYED))
        );
    }

//    @Override
//    public List<PublishedCommand> findReplayedOnSecondary() {
//        return _Casts.uncheckedCast( repositoryService().allMatches(
//                Query.named(publishedCommandClass, PublishedCommand.NAMED_QUERY_FIND_REPLAYABLE_MOST_RECENT_STARTED))
//        );
//    }

    @Override
    public List<PublishedCommand> saveForReplay(final CommandsDto commandsDto) {
        List<CommandDto> commandDto = commandsDto.getCommandDto();
        List<PublishedCommand> commands = new ArrayList<>();
        for (final CommandDto dto : commandDto) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }

    @Override
    public PublishedCommand saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final PublishedCommand publishedCommand = newPublishedCommand();

        publishedCommand.setInteractionId(UUID.fromString(dto.getInteractionId()));
        publishedCommand.setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(dto.getTimestamp()));
        publishedCommand.setUsername(dto.getUser());

        publishedCommand.setReplayState(ReplayState.PENDING);

        final OidDto firstTarget = dto.getTargets().getOid().get(0);
        publishedCommand.setTarget(Bookmark.forOidDto(firstTarget));
        publishedCommand.setCommandDto(dto);
        publishedCommand.setLogicalMemberIdentifier(dto.getMember().getLogicalMemberIdentifier());

        persist(publishedCommand);

        return publishedCommand;
    }

    @Override
    public void persist(final PublishedCommand publishedCommand) {
        repositoryService().persist(publishedCommand);
    }

    @Override
    public void truncateLog() {
        repositoryService().removeAll(PublishedCommand.class);
    }

    public PublishedCommand newPublishedCommand() {
        return _InstanceUtil.createInstance(publishedCommandClass, PublishedCommand.class);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

}

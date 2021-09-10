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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommandRepository;
import org.apache.isis.extensions.commandlog.applib.dom.ReplayState;
import org.apache.isis.persistence.jdo.applib.services.JdoSupportService;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;
import org.apache.isis.schema.cmd.v2.MapDto;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.common.v2.OidDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides supporting functionality for querying and persisting
 * {@link PublishedCommandForJdo command} entities.
 */
@Service
@Named("isis.ext.commandLog.CommandJdoRepository")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jdo")
@RequiredArgsConstructor
//@Log4j2
public class CommandJdoRepository
implements PublishedCommandRepository<PublishedCommandForJdo> {

    @Inject final Provider<InteractionProvider> interactionProviderProvider;
    @Inject final Provider<RepositoryService> repositoryServiceProvider;
    @Inject final JdoSupportService jdoSupport;

    @Override
    public List<PublishedCommandForJdo> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<PublishedCommandForJdo> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(PublishedCommandForJdo.class, "findByTimestampBetween")
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(PublishedCommandForJdo.class, "findByTimestampAfter")
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(PublishedCommandForJdo.class, "findByTimestampBefore")
                        .withParameter("to", toTs);
            } else {
                query = Query.named(PublishedCommandForJdo.class, "find");
            }
        }
        return repositoryService().allMatches(query);
    }

    @Override
    public Optional<PublishedCommandForJdo> findByInteractionId(final UUID interactionId) {
        return repositoryService().firstMatch(
                Query.named(PublishedCommandForJdo.class, "findByInteractionIdStr")
                    .withParameter("interactionIdStr", interactionId.toString()));
    }

    @Override
    public List<PublishedCommandForJdo> findByParent(final CommandModel parent) {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findByParent")
                    .withParameter("parent", parent));
    }

    @Override
    public List<PublishedCommandForJdo> findCurrent() {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findCurrent"));
    }

    @Override
    public List<PublishedCommandForJdo> findCompleted() {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findCompleted"));
    }

    @Override
    public List<PublishedCommandForJdo> findByTargetAndFromAndTo(
            final Bookmark target,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {

        final Timestamp fromTs = toTimestampStartOfDayWithOffset(from, 0);
        final Timestamp toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<PublishedCommandForJdo> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(PublishedCommandForJdo.class, "findByTargetAndTimestampBetween")
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(PublishedCommandForJdo.class, "findByTargetAndTimestampAfter")
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(PublishedCommandForJdo.class, "findByTargetAndTimestampBefore")
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(PublishedCommandForJdo.class, "findByTarget")
                        .withParameter("target", target);
            }
        }
        return repositoryService().allMatches(query);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(
            final @Nullable LocalDate dt,
            final int daysOffset) {

        return dt!=null
                ? new java.sql.Timestamp(
                        Instant.from(dt.atStartOfDay().plusDays(daysOffset).atZone(ZoneId.systemDefault()))
                        .toEpochMilli())
                : null;
    }

    @Override
    public List<PublishedCommandForJdo> findRecentByUsername(final String username) {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findRecentByUsername")
                    .withParameter("username", username));
    }

    @Override
    public List<PublishedCommandForJdo> findRecentByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findRecentByTarget")
                    .withParameter("target", target));
    }

    @Override
    public List<PublishedCommandForJdo> findSince(final UUID interactionId, final Integer batchSize) {
        if(interactionId == null) {
            return findFirst();
        }
        final PublishedCommandForJdo from = findByInteractionIdElseNull(interactionId);
        if(from == null) {
            return Collections.emptyList();
        }
        return findSince(from.getTimestamp(), batchSize);
    }

    private List<PublishedCommandForJdo> findFirst() {
        Optional<PublishedCommandForJdo> firstCommandIfAny = repositoryService().firstMatch(
                Query.named(PublishedCommandForJdo.class, "findFirst"));
        return firstCommandIfAny
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }


    private PublishedCommandForJdo findByInteractionIdElseNull(final UUID interactionId) {
        val tsq = jdoSupport.newTypesafeQuery(PublishedCommandForJdo.class);
        val cand = QCommandJdo.candidate();
        val q = tsq.filter(
                cand.interactionIdStr.eq(tsq.parameter("interactionIdStr", String.class))
        );
        q.setParameter("interactionIdStr", interactionId.toString());
        return q.executeUnique();
    }

    private List<PublishedCommandForJdo> findSince(
            final Timestamp timestamp,
            final Integer batchSize) {

        // DN generates incorrect SQL for SQL Server if count set to 1; so we set to 2 and then trim
        // XXX that's a historic workaround, should rather be fixed upstream
        val needsTrimFix = batchSize != null && batchSize == 1;

        val q = Query.named(PublishedCommandForJdo.class, "findSince")
                .withParameter("timestamp", timestamp)
                .withRange(QueryRange.limit(
                        needsTrimFix ? 2L : batchSize
                ));

        final List<PublishedCommandForJdo> publishedCommands = repositoryService().allMatches(q);
        return needsTrimFix && publishedCommands.size() > 1
                    ? publishedCommands.subList(0,1)
                    : publishedCommands;
    }


    @Override
    public Optional<PublishedCommandForJdo> findMostRecentReplayed() {

        return repositoryService().firstMatch(
                Query.named(PublishedCommandForJdo.class, "findMostRecentReplayed"));
    }

    @Override
    public Optional<PublishedCommandForJdo> findMostRecentCompleted() {
        return repositoryService().firstMatch(
                Query.named(PublishedCommandForJdo.class, "findMostRecentCompleted"));
    }

    @Override
    public List<PublishedCommandForJdo> findNotYetReplayed() {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findNotYetReplayed"));
    }

    @Override
    public List<PublishedCommandForJdo> findReplayedOnSecondary() {
        return repositoryService().allMatches(
                Query.named(PublishedCommandForJdo.class, "findReplayableMostRecentStarted"));
    }

    @Override
    public List<PublishedCommandForJdo> saveForReplay(final CommandsDto commandsDto) {
        List<CommandDto> commandDto = commandsDto.getCommandDto();
        List<PublishedCommandForJdo> commands = new ArrayList<>();
        for (final CommandDto dto : commandDto) {
            commands.add(saveForReplay(dto));
        }
        return commands;
    }

    @Programmatic
    @Override
    public PublishedCommandForJdo saveForReplay(final CommandDto dto) {

        if(dto.getMember().getInteractionType() == InteractionType.ACTION_INVOCATION) {
            final MapDto userData = dto.getUserData();
            if (userData == null ) {
                throw new IllegalStateException(String.format(
                        "Can only persist action DTOs with additional userData; got: \n%s",
                        CommandDtoUtils.toXml(dto)));
            }
        }

        final PublishedCommandForJdo publishedCommand = new PublishedCommandForJdo();

        publishedCommand.setInteractionIdStr(dto.getInteractionId());
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
    public void persist(final PublishedCommandForJdo publishedCommand) {
        repositoryService().persist(publishedCommand);
    }

    @Override
    public void truncateLog() {
        repositoryService().removeAll(PublishedCommandForJdo.class);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

}

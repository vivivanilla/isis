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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.CommandsDto;

public interface PublishedCommandRepository {

    Optional<PublishedCommand> findByInteractionId(UUID interactionId);

    List<PublishedCommand> findByParent(PublishedCommand parent);

    List<PublishedCommand> findByFromAndTo(LocalDate from, LocalDate to);

    List<PublishedCommand> findCurrent();

    List<PublishedCommand> findCompleted();

    List<PublishedCommand> findByTargetAndFromAndTo(Bookmark target, LocalDate from, LocalDate to);

    List<PublishedCommand> findRecentByUsername(String username);

    List<PublishedCommand> findRecentByTarget(Bookmark target);

    /**
     * Intended to support the replay of commands on a secondary instance of
     * the application.
     *
     * This finder returns all (completed) {@link PublishedCommand}s started after
     * the command with the specified interactionId.  The number of commands
     * returned can be limited so that they can be applied in batches.
     *
     * If the provided interactionId is null, then only a single
     * {@link PublishedCommand command} is returned.  This is intended to support
     * the case when the secondary does not yet have any
     * {@link PublishedCommand command}s replicated.  In practice this is unlikely;
     * typically we expect that the secondary will be set up to run against a
     * copy of the primary instance's DB (restored from a backup), in which
     * case there will already be a {@link PublishedCommand command} representing the
     * current high water mark on the secondary system.
     *
     * If the interactionId is not null but the corresponding
     * {@link PublishedCommand command} is not found, then <tt>null</tt> is returned.
     * In the replay scenario the caller will probably interpret this as an
     * error because it means that the high water mark on the secondary is
     * inaccurate, referring to a non-existent {@link PublishedCommand command} on
     * the primary.
     *
     * @param interactionId - the identifier of the {@link PublishedCommand command} being
     *                   the replay hwm (using {@link #findMostRecentReplayed()} on the
     *                   secondary), or null if no HWM was found there.
     * @param batchSize - to restrict the number returned (so that replay
     *                   commands can be batched).
     */
    List<PublishedCommand> findSince(UUID interactionId, Integer batchSize);

    /**
     * The most recent replayed command previously replicated from primary to
     * secondary.
     *
     * <p>
     * This should always exist except for the very first times
     * (after restored the prod DB to secondary).
     * </p>
     */
    Optional<PublishedCommand> findMostRecentReplayed();

    /**
     * The most recent completed command, as queried on the
     * secondary.
     *
     * <p>
     *     After a restart following the production database being restored
     *     from primary to secondary, would correspond to the last command
     *     run on primary before the production database was restored to the
     *     secondary.
     * </p>
     */
    Optional<PublishedCommand> findMostRecentCompleted();

    List<PublishedCommand> findNotYetReplayed();

    // List<PublishedCommand> findReplayedOnSecondary();

    PublishedCommand saveForReplay(CommandDto dto);

    List<PublishedCommand> saveForReplay(CommandsDto commandsDto);

    void persist(PublishedCommand commandJdo);

    void truncateLog();


}

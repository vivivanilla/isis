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
package org.apache.isis.extensions.executionoutbox.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.Getter;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionOutboxEntry command} entities.
 */
public abstract class ExecutionOutboxEntryRepository<E extends ExecutionOutboxEntry> {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtExecutionOutboxApplib.NAMESPACE + ".ExecutionOutboxEntryRepository";

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Execution log entry not found");
            this.interactionId = interactionId;
        }
    }

    private final Class<E> executionOutboxEntryClass;

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;
    @Inject IsisSystemEnvironment isisSystemEnvironment;

    protected ExecutionOutboxEntryRepository(Class<E> executionOutboxEntryClass) {
        this.executionOutboxEntryClass = executionOutboxEntryClass;
    }


    /**
     * for testing only.
     */
    protected ExecutionOutboxEntryRepository(Class<E> executionOutboxEntryClass, Provider<RepositoryService> repositoryServiceProvider, FactoryService factoryService) {
        this.executionOutboxEntryClass = executionOutboxEntryClass;
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.factoryService = factoryService;
    }

    public E createEntryAndPersist(final Execution execution) {
        E e = factoryService.detachedEntity(executionOutboxEntryClass);
        e.init(execution);
        persist(e);
        return e;
    }

    public Optional<E> findByInteractionIdAndSequence(final UUID interactionId, final int sequence) {
        return repositoryService().firstMatch(
                Query.named(executionOutboxEntryClass,  ExecutionOutboxEntry.Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE)
                        .withParameter("interactionId", interactionId)
                        .withParameter("sequence", sequence)
        );
    }

    public List<E> findOldest() {
        return repositoryService().allMatches(
                Query.named(executionOutboxEntryClass, ExecutionOutboxEntry.Nq.FIND_OLDEST)
                        .withLimit(100));
    }

    public ExecutionOutboxEntry upsert(
            final UUID interactionId,
            final int sequence,
            final ExecutionOutboxEntryType executionType,
            final Timestamp startedAt,
            final String username,
            final Bookmark target,
            final String logicalMemberIdentifier,
            final String xml) {
        return upsert(interactionId, sequence, executionType, startedAt, username, target, logicalMemberIdentifier, InteractionDtoUtils.fromXml(xml));
    }

    public ExecutionOutboxEntry upsert(
            final UUID interactionId,
            final int sequence,
            final ExecutionOutboxEntryType executionType,
            final Timestamp startedAt,
            final String username,
            final Bookmark target,
            final String logicalMemberIdentifier,
            final InteractionDto interactionDto) {

        return findByInteractionIdAndSequence(interactionId, sequence)
                .orElseGet(() -> {

                    E outboxEvent = factoryService.detachedEntity(executionOutboxEntryClass);

                    outboxEvent.setExecutionType(executionType);
                    outboxEvent.setInteractionId(interactionId);
                    outboxEvent.setTimestamp(startedAt);
                    outboxEvent.setSequence(sequence);
                    outboxEvent.setUsername(username);

                    outboxEvent.setTarget(target);
                    outboxEvent.setLogicalMemberIdentifier(logicalMemberIdentifier);

                    outboxEvent.setInteractionDto(interactionDto);

                    repositoryService().persist(outboxEvent);

                    return outboxEvent;
                });
    }

    protected abstract E newExecutionOutboxEntry();

    @Programmatic
    public boolean deleteByInteractionIdAndSequence(final UUID interactionId, final int sequence) {
        Optional<E> outboxEventIfAny = findByInteractionIdAndSequence(interactionId, sequence);
        if(outboxEventIfAny.isPresent()) {
            repositoryService().removeAndFlush(outboxEventIfAny.get());
            return true;
        } else {
            return false;
        }
    }

    private void persist(final E commandLogEntry) {
        repositoryService().persist(commandLogEntry);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }



    /**
     * for testing purposes only
     */
    public List<E> findAll() {
        if (isisSystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot removeAll in production systems");
        }
        return repositoryService().allInstances(executionOutboxEntryClass);
    }

    /**
     * for testing purposes only
     */
    public void removeAll() {
        if (isisSystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot removeAll in production systems");
        }
        repositoryService().removeAll(executionOutboxEntryClass);
    }

}

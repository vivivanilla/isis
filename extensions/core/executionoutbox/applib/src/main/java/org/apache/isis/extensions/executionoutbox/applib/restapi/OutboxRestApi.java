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
package org.apache.isis.extensions.executionoutbox.applib.restapi;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.util.schema.InteractionsDtoUtils;
import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntryRepository;
import org.apache.isis.extensions.executionoutbox.applib.spiimpl.ContentMappingServiceForOutboxEvents;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Named(OutboxRestApi.LOGICAL_TYPE_NAME)
@DomainService(nature = NatureOfService.REST)
@RequiredArgsConstructor
public class OutboxRestApi  {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtExecutionOutboxApplib.NAMESPACE + ".OutboxRestApi";

    final @Inject ExecutionOutboxEntryRepository<? extends ExecutionOutboxEntry> entryRepository;

    /**
     * This action is intended to be invoked with <code>Accept</code> header set to
     * <code>application/xml;profile=urn:org.restfulobjects:repr-types/action-result;x-ro-domain-type=org.apache.isis.schema.ixn.v2.InteractionsDto</code>
     *
     * <p>
     *     The {@link ContentMappingServiceForOutboxEvents} will then serialize the resultant {@link OutboxEvents} view model into XML.
     * </p>
     *
     * @return
     */
    @Action(
            semantics = SemanticsOf.SAFE,
            executionPublishing = Publishing.DISABLED,
            commandPublishing = Publishing.DISABLED
    )
    public OutboxEvents pending() {
        val outboxEvents = factoryService.viewModel(new OutboxEvents());
        List<? extends ExecutionOutboxEntry> entries = entryRepository.findOldest();
        outboxEvents.getExecutions().addAll(entries);
        return outboxEvents;
    }

    @Action(
            semantics = SemanticsOf.IDEMPOTENT,
            executionPublishing = Publishing.DISABLED,
            commandPublishing = Publishing.DISABLED
    )
    public void delete(final String interactionId, final int sequence) {
        entryRepository.deleteByInteractionIdAndSequence(UUID.fromString(interactionId), sequence);
    }

    @Action(
            semantics = SemanticsOf.IDEMPOTENT,
            executionPublishing = Publishing.DISABLED,
            commandPublishing = Publishing.DISABLED
    )
    public void deleteMany(final String interactionsDtoXml) {
        val interactionsDto = InteractionsDtoUtils.fromXml(interactionsDtoXml);
        interactionsDto.getInteractionDto().
                forEach(interactionType -> {
                    val interactionId = interactionType.getInteractionId();
                    val sequence = interactionType.getExecution().getSequence();
                    entryRepository.deleteByInteractionIdAndSequence(UUID.fromString(interactionId), sequence);
                });
    }

    @Inject FactoryService factoryService;

}

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
package org.apache.isis.extensions.commandlog.applib.subscriptions;

import java.util.UUID;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.util.JaxbUtil;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommandRepositoryAbstract;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandLog.PublishedCommandSubscriber")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT) // after JdoPersistenceLifecycleService
@Qualifier("Default")
@Log4j2
@RequiredArgsConstructor
public class PublishedCommandSubscriber implements CommandSubscriber {

    final PublishedCommandRepositoryAbstract<?> publishedCommandRepository;

    @Override
    public void onCompleted(Command command) {

        if(!command.isSystemStateChanged()) {
            return;
        }

        var commandInteractionId = command.getInteractionId();
        final var existingCommandIfAny =
                publishedCommandRepository.findByInteractionId(commandInteractionId);

        if(existingCommandIfAny.isPresent()) {
            // this isn't expected to happen; we just log the fact if it does
            log.warn("Unexpectedly found existing command with interactionId '{}'; ignoring", commandInteractionId);

            if(log.isDebugEnabled()) {

                val existingCommandDto = existingCommandIfAny.get().getCommandDto();

                val existingCommandDtoXml = JaxbUtil.toXml(existingCommandDto).presentElse("Could not extract XML for existing command");
                val commandDtoXml = JaxbUtil.toXml(command.getCommandDto()).presentElse("Could not extract XML for received command");

                log.debug("existing: \n{}", existingCommandDtoXml);
                log.debug("received: \n{}", commandDtoXml);
            }
            return;
        }

        val publishedCommand = publishedCommandRepository.newPublishedCommand();
        val parent = command.getParent();
        val parentCommand =
            parent != null
                ? publishedCommandRepository
                    .findByInteractionId(parent.getInteractionId())
                    .orElse(null)
                : null;
        publishedCommand.setParent(parentCommand);
        publishedCommandRepository.persist(publishedCommand);
    }


}

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
package org.apache.isis.applib.services.publishing.log;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.util.schema.CommandDtoUtils;

import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * Simple implementation of {@link CommandSubscriber} that just logs out the {@link Command Command}'s
 * {@link Command#getCommandDto() DTO} to a debug log.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CommandLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("Logging")
@Log4j2
public class CommandLogger implements CommandSubscriber {

    static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".CommandLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void onCompleted(Command command) {

        val commandDto = command.getCommandDto();
        val xml = CommandDtoUtils.toXml(commandDto);

        log.debug("completed: {}, systemStateChanged {} \n{}",
                command.getLogicalMemberIdentifier(),
                command.isSystemStateChanged(),
                xml);
    }

}

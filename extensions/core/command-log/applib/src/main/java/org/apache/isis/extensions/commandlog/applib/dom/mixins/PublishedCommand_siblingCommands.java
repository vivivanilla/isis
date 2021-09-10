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
package org.apache.isis.extensions.commandlog.applib.dom.mixins;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommandRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Collection(
    domainEvent = PublishedCommand_siblingCommands.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    sequence = "100.110"
)
@RequiredArgsConstructor
public class PublishedCommand_siblingCommands {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<PublishedCommand_siblingCommands, PublishedCommand> { }

    private final PublishedCommand publishedCommand;

    public List<PublishedCommand> coll() {
        val parentCommand = publishedCommand.getParent();
        if(parentCommand == null) {
            return Collections.emptyList();
        }
        val siblingCommands = repository.findByParent(parentCommand);
        siblingCommands.remove(publishedCommand);
        return siblingCommands;
    }


    @Inject PublishedCommandRepository repository;

}

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
package org.apache.isis.extensions.commandlog.applib.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommandRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@DomainService(
    nature = NatureOfService.VIEW,
    logicalTypeName = CommandServiceMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
    named = "Activity",
    menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@Named(CommandServiceMenu.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandServiceMenu {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogApplib.NAMESPACE + ".CommandServiceMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtCommandLogApplib.ActionDomainEvent<T> { }

    final ClockService clockService;
    final PublishedCommandRepository publishedCommandRepository;


    @Action(domainEvent = activeCommands.DomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, cssClassFa = "fa-bolt", sequence="10")
    public class activeCommands {
        public class DomainEvent extends ActionDomainEvent<CommandServiceMenu.activeCommands> { }

        @MemberSupport public List<PublishedCommand> act() {
            return publishedCommandRepository.findCurrent();
        }
        @MemberSupport public boolean hideAct() {
            return publishedCommandRepository == null;
        }
    }


    @Action(domainEvent = findCommands.DomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-search", sequence="20")
    public class findCommands {
        public class DomainEvent extends ActionDomainEvent<CommandServiceMenu.findCommands> { }

        @MemberSupport public List<PublishedCommand> act(
                final @Nullable LocalDate from,
                final @Nullable LocalDate to) {
            return publishedCommandRepository.findByFromAndTo(from, to);
        }
        @MemberSupport public boolean hideAct() {
            return publishedCommandRepository == null;
        }
        @MemberSupport public LocalDate default0Act() {
            return now().minusDays(7);
        }
        @MemberSupport public LocalDate default1Act() {
            return now();
        }
    }


    @Action(domainEvent = findCommandById.DomainEvent.class, semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa = "fa-crosshairs", sequence="30")
    public class findCommandById {
        public class DomainEvent extends ActionDomainEvent<CommandServiceMenu.findCommandById> { }

        @MemberSupport public PublishedCommand act(
                final UUID transactionId) {
            return publishedCommandRepository.findByInteractionId(transactionId).orElse(null);
        }
        @MemberSupport public boolean hideAct() {
            return publishedCommandRepository == null;
        }
    }


    @Action(domainEvent = truncateLog.DomainEvent.class, semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE, restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(cssClassFa = "fa-trash", sequence="40")
    public class truncateLog {
        public class DomainEvent extends ActionDomainEvent<CommandServiceMenu.truncateLog> { }

        @MemberSupport public void act() {
            publishedCommandRepository.truncateLog();
        }
    }

    private LocalDate now() {
        return clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
    }
}


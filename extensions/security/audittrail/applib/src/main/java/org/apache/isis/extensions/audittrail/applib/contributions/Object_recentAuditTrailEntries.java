/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.isis.extensions.audittrail.applib.contributions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;


import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.extensions.audittrail.applib.IsisModuleExtAuditTrailApplib;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.isis.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = Object_recentAuditTrailEntries.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE
)
@RequiredArgsConstructor
public class Object_recentAuditTrailEntries {

    private final Object domainObject;

    public static class ActionDomainEvent
            extends IsisModuleExtAuditTrailApplib.ActionDomainEvent<Object_recentAuditTrailEntries> {}

    @MemberSupport public List<? extends AuditTrailEntry> act(
            final String propertyName) {
        val target = bookmarkService.bookmarkForElseFail(domainObject);
        return auditTrailEntryRepository.findRecentByTargetAndPropertyId(target, propertyName);
    }
    @MemberSupport public List<String> choices0Act() {
        val domainClass = domainObject.getClass();
        val logicalTypeIfAny = metaModelService.lookupLogicalTypeByClass(domainClass);
        if(logicalTypeIfAny.isEmpty()) {
            // not expected, due to hide guard
            return Collections.emptyList();
        }
        val propertyFeatureIds = applicationFeatureRepository.propertyIdsFor(logicalTypeIfAny.get());
        return propertyFeatureIds.stream().map(ApplicationFeatureId::getLogicalMemberName).collect(Collectors.toList());
    }
    @MemberSupport public String default0Act() {
        val choices = choices0Act();
        return choices.size() == 1 ? choices.get(0): null;
    }
    @MemberSupport public boolean hideAct() {
        val domainClass = domainObject.getClass();
        val logicalTypeIfAny = metaModelService.lookupLogicalTypeByClass(domainClass);
        return metaModelService.lookupLogicalTypeByClass(domainClass).isEmpty();
    }

    @Inject MetaModelService metaModelService;
    @Inject ApplicationFeatureRepository applicationFeatureRepository;
    @Inject AuditTrailEntryRepository<? extends AuditTrailEntry> auditTrailEntryRepository;
    @Inject BookmarkService bookmarkService;
}

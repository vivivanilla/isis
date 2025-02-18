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
package demoapp.dom.domain.properties.Property.domainEvent.subscribers;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.properties.Property.domainEvent.PropertyDomainEventVm;

// tag::class[]
@Service
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
class PropertyDomainEventControlService {

    final ServiceRegistry serviceRegistry;

    PropertyDomainEventControlStrategy controlStrategy = PropertyDomainEventControlStrategy.DO_NOTHING;   // <.>

    @EventListener(PropertyDomainEventVm.TextDomainEvent.class)     // <.>
    public void on(PropertyDomainEventVm.TextDomainEvent ev) {
        controlStrategy.on(ev, serviceRegistry);
    }

}
// end::class[]

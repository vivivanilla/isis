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
package org.apache.isis.core.runtimeservices.eventbus;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".EventBusServiceSpring")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Spring")
public class EventBusServiceSpring implements EventBusService {

    @Inject private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void post(Object event) {
        applicationEventPublisher.publishEvent(event);
    }

}

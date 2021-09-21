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
package org.apache.isis.extensions.commandlog.jdo.entities;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommandRepositoryAbstract;
import org.apache.isis.persistence.jdo.applib.services.JdoSupportService;

import lombok.RequiredArgsConstructor;

/**
 * Provides supporting functionality for querying and persisting
 * {@link PublishedCommandForJdo command} entities.
 */
@Service
@Named("isis.ext.commandLog.PublishedCommandRepository")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Jdo")
public class PublishedCommandForJdoRepository
extends PublishedCommandRepositoryAbstract<PublishedCommandForJdo> {

    @Inject JdoSupportService jdoSupport;

    public PublishedCommandForJdoRepository() {
        super(PublishedCommandForJdo.class);
    }


    @Override protected PublishedCommand newPublishedCommand() {
        return new PublishedCommandForJdo();
    }
}

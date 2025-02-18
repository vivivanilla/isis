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
package org.apache.isis.persistence.jpa.integration.changetracking;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.metrics.MetricsService;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.transaction.PersistenceMetricsServiceJpa")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("jpa")
//@Log4j2
public class PersistenceMetricsServiceJpa
implements
    MetricsService {

    // -- METRICS

    @Override
    public int numberEntitiesLoaded() {
        return -1; // n/a
    }

    @Override
    public int numberEntitiesDirtied() {
        return -1; // n/a
    }

}

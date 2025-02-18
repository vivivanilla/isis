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
package org.apache.isis.extensions.executionoutbox.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.executionoutbox.applib.IsisModuleExtExecutionOutboxApplib;
import org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntryPK;
import org.apache.isis.extensions.executionoutbox.jpa.dom.ExecutionOutboxEntryRepository;
import org.apache.isis.persistence.jpa.eclipselink.IsisModulePersistenceJpaEclipselink;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jpa.TeardownFixtureJpaAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,
        IsisModuleExtExecutionOutboxApplib.class,
        IsisModulePersistenceJpaEclipselink.class,

        // @Service's
        ExecutionOutboxEntryRepository.class,
        ExecutionOutboxEntryPK.Stringifier.class,

        // entities
        ExecutionOutboxEntry.class
})
@EntityScan(basePackageClasses = {
        ExecutionOutboxEntry.class
})
public class IsisModuleExtExecutionOutboxPersistenceJpa implements ModuleWithFixtures {

    public static final String NAMESPACE = IsisModuleExtExecutionOutboxApplib.NAMESPACE;
    public static final String SCHEMA = IsisModuleExtExecutionOutboxApplib.SCHEMA;

    @Override
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureJpaAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(ExecutionOutboxEntry.class);
            }
        };
    }

}

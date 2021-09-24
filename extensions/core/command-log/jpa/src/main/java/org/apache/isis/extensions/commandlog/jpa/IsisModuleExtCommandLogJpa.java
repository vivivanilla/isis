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
package org.apache.isis.extensions.commandlog.jpa;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.jpa.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.jpa.dom.PublishedCommandRepository;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // module dependencies
        IsisModuleExtCommandLogApplib.class

        // @DomainService's
        , PublishedCommandRepository.class

        // entities
        , PublishedCommand.class
})
@ComponentScan(
        basePackageClasses= {
                IsisModuleExtCommandLogJpa.class
        })
public class IsisModuleExtCommandLogJpa {

    /**
     * For tests that need to delete the command table first.
     * Should be run in the <code>@Before</code> of the test.
     *
     * <p>
     *     NOTE: this class deliberately does <i>not</i> implement {@link ModuleWithFixtures}.
     * </p>
     */
    public FixtureScript getTeardownFixture() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(PublishedCommand.class);
            }
        };
    }

}

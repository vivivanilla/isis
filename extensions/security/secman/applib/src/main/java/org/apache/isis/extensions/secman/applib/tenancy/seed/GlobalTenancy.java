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
package org.apache.isis.extensions.secman.applib.tenancy.seed;

import org.apache.isis.extensions.secman.applib.tenancy.fixtures.AbstractTenancyFixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

/**
 * @since 2.0 {@index}
 */
public class GlobalTenancy extends AbstractTenancyFixtureScript {

    public static final String TENANCY_NAME = "Global";
    public static final String TENANCY_PATH = "/";

    @Override
    protected void execute(FixtureScript.ExecutionContext executionContext) {
        create(TENANCY_NAME, TENANCY_PATH, null, executionContext);
    }

}

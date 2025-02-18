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
package org.apache.isis.applib.types;

import org.apache.isis.applib.services.command.Command;

import lombok.experimental.UtilityClass;

/**
 * A user-friendly name of a class, as per {@link Command#getTarget()},
 * {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber#onChanging(org.apache.isis.applib.services.publishing.spi.EntityPropertyChange)}.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class TargetClassType {

    @UtilityClass
    public static class Meta {
        public static final int MAX_LEN = 50;
    }

}

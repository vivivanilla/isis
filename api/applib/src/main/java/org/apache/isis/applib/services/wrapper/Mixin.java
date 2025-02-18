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
package org.apache.isis.applib.services.wrapper;

import org.apache.isis.applib.services.wrapper.control.SyncControl;

/**
 * Marker interface that mixins can optionally implement, to indicate the type of the mixee that they require.
 *
 * <p>
 *     Use with {@link WrapperFactory} methods (eg {@link WrapperFactory#wrapMixinT(Class, Object)}), eg when writing
 *     integration tests, to ensure that the correct mixee is passed into the method.
 * </p>
 *
 * @param <MIXEE>
 */
public interface Mixin<MIXEE> {

}

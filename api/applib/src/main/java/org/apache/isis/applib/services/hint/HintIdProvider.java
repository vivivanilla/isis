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
package org.apache.isis.applib.services.hint;

/**
 * Provides a SPI for view models to implement to represent
 * their "logical" identity (stable even if the view model's state changes).
 *
 * <p>
 *     Hints are stored against the `Bookmark` of a domain object, essentially
 *     the identifier of the domain object. For a domain entity this identifier
 *     is fixed and unchanging but for view models the identifier changes each
 *     time the view model's state changes (the identifier is basically a
 *     digest of the object's state).
 *     This means that any hints stored against the view model's bookmark are
 *     in effect lost as soon as the view model is modified.
 * </p>
 *
 * <p>
 *     This SPI therefore allows a view model to take advantage of the hinting
 *     mechanism of the viewer by providing a "logical" identity stored which
 *     hints for the view model can be stored.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface HintIdProvider {
    String hintId();
}

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
package org.apache.isis.extensions.commandlog.applib;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.commandlog.applib.app.CommandServiceMenu;
import org.apache.isis.extensions.commandlog.applib.dom.PublishedCommand;
import org.apache.isis.extensions.commandlog.applib.subscriptions.PublishedCommandSubscriber;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleTestingFixturesApplib.class,

        // @Services
        CommandServiceMenu.class,
        PublishedCommandSubscriber.class,
        PublishedCommand.TitleProvider.class,
        PublishedCommand.TableColumnOrderDefault.class,

})
public class IsisModuleExtCommandLogApplib {

    public static final String NAMESPACE = "isis.ext.commandLog";

    public static abstract class TitleUiEvent<S>
        extends org.apache.isis.applib.events.ui.TitleUiEvent<S> { }

    public static abstract class IconUiEvent<S>
        extends org.apache.isis.applib.events.ui.IconUiEvent<S> { }

    public static abstract class CssClassUiEvent<S>
        extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> { }

    public static abstract class LayoutUiEvent<S>
        extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> { }

    public static abstract class ActionDomainEvent<S>
        extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public static abstract class CollectionDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public static abstract class PropertyDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

}

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
package org.apache.isis.extensions.executionoutbox.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.executionoutbox.applib.dom.ExecutionOutboxEntry;
import org.apache.isis.extensions.executionoutbox.applib.restapi.OutboxRestApi;
import org.apache.isis.extensions.executionoutbox.applib.spiimpl.ContentMappingServiceForOutboxEvents;
import org.apache.isis.extensions.executionoutbox.applib.spiimpl.ExecutionSubscriberForExecutionOutbox;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;

@Configuration
@Import({
        // @DomainService's
        OutboxRestApi.class,

        // @Service's
        ExecutionSubscriberForExecutionOutbox.class,
        ExecutionOutboxEntry.TableColumnOrderDefault.class,
        ContentMappingServiceForOutboxEvents.class
})
public class IsisModuleExtExecutionOutboxApplib
implements ModuleWithFixtures {

    public static final String NAMESPACE = "isis.ext.executionOutbox";
    public static final String SCHEMA = "isisExtExecutionOutbox";

    public abstract static class TitleUiEvent<S>
        extends org.apache.isis.applib.events.ui.TitleUiEvent<S> { }

    public abstract static class IconUiEvent<S>
        extends org.apache.isis.applib.events.ui.IconUiEvent<S> { }

    public abstract static class CssClassUiEvent<S>
        extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> { }

    public abstract static class LayoutUiEvent<S>
        extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> { }

    public abstract static class ActionDomainEvent<S>
        extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
        extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }


}

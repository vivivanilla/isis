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
package demoapp.dom.domain.properties.Property.commandPublishing;

import javax.inject.Named;

import org.apache.isis.applib.annotation.DomainObject;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;
import demoapp.dom.domain._commands.ExposePersistedCommands;

@Named("demo.PropertyCommandPublishingEntity") // shared permissions with concrete sub class
@DomainObject
public abstract class PropertyCommandPublishingEntity
implements
    HasAsciiDocDescription,
    ExposePersistedCommands,
    ValueHolder<String> {

    @Override
    public String value() {
        return getProperty();
    }

    protected abstract String getProperty();
    protected abstract void setProperty(String value);

    protected abstract String getPropertyCommandPublishingDisabled();
    protected abstract void setPropertyCommandPublishingDisabled(String value);

    protected abstract String getPropertyMetaAnnotated();
    protected abstract void setPropertyMetaAnnotated(String value);

    protected abstract String getPropertyMetaAnnotatedOverridden();
    protected abstract void setPropertyMetaAnnotatedOverridden(String value);

}
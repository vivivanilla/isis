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
package org.apache.isis.core.metamodel.facets;

import java.util.Set;

import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.id.LogicalType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;

public class DomainEventHelperTest_newCollectionDomainEvent_forRemove {

    public static class SomeDomainObject {
        public Set<SomeReferencedObject> getReferences() { return null; }
    }
    public static class SomeReferencedObject {}

    public static class SomeDomainObjectCollectionRemovedFromDomainEvent extends CollectionDomainEvent<SomeDomainObject, SomeReferencedObject> { }

    @Test
    public void defaultEventType() throws Exception {
        SomeDomainObject sdo = new SomeDomainObject();
        SomeReferencedObject other = new SomeReferencedObject();
        Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<Object, Object> ev = Utils.domainEventHelper().newCollectionDomainEvent(
                CollectionDomainEvent.Default.class, AbstractDomainEvent.Phase.EXECUTED, identifier, sdo);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
    }

    @Test
    public void collectionRemovedFromDefaultEventType() throws Exception {
        SomeDomainObject sdo = new SomeDomainObject();
        SomeReferencedObject other = new SomeReferencedObject();
        Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<Object, Object> ev = Utils.domainEventHelper().newCollectionDomainEvent(
                CollectionDomainEvent.Default.class, AbstractDomainEvent.Phase.EXECUTED, identifier, sdo);
        assertSame(ev.getSource(), sdo);
        assertThat(ev.getIdentifier(), is(identifier));
    }

    @Test
    public void customEventType() throws Exception {
        SomeDomainObject sdo = new SomeDomainObject();
        SomeReferencedObject other = new SomeReferencedObject();
        Identifier identifier = Identifier.collectionIdentifier(
                LogicalType.fqcn(SomeDomainObject.class), "references");

        final CollectionDomainEvent<SomeDomainObject, SomeReferencedObject> ev = Utils.domainEventHelper().newCollectionDomainEvent(
                SomeDomainObjectCollectionRemovedFromDomainEvent.class, AbstractDomainEvent.Phase.EXECUTED, identifier, sdo);
        assertThat(ev.getSource(), is(sdo));
        assertThat(ev.getIdentifier(), is(identifier));
    }

}

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
package org.apache.isis.core.metamodel.facets.object.value;

import org.apache.isis.commons.internal.delegate._Delegate;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

import lombok.val;

public class CompositeValueUpdaterForProperty
extends CompositeValueUpdater {

    public static ObjectAction createProxy(
            final ManagedProperty managedProperty,
            final ObjectActionMixedIn delegate) {
        return _Delegate.createProxy(ObjectAction.class,
                new CompositeValueUpdaterForProperty(managedProperty, delegate));
    }

    private final ManagedProperty managedProperty;

    protected CompositeValueUpdaterForProperty(
            final ManagedProperty managedProperty,
            final ObjectActionMixedIn delegate) {
        super(delegate);
        this.managedProperty = managedProperty;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return managedProperty.getElementType();
    }

    @Override
    protected ManagedObject map(final ManagedObject valueType) {
        val propNeg = managedProperty.startNegotiation();
        propNeg.getValue().setValue(valueType);
        propNeg.submit();
        return managedProperty.getOwner();
    }

}

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
package org.apache.isis.core.metamodel.facets.properties.choices.enums;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacetAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class PropertyChoicesFacetFromChoicesFacet
extends PropertyChoicesFacetAbstract {

    public static Optional<PropertyChoicesFacetAbstract> create(
            final Optional<ChoicesFacet> choicesFacet,
            final FacetHolder facetHolder) {
        return choicesFacet
        .map(choicesFct->new PropertyChoicesFacetFromChoicesFacet(choicesFct, facetHolder));
    }

    private final ChoicesFacet choicesFacet;

    private PropertyChoicesFacetFromChoicesFacet(
            final ChoicesFacet choicesFacet,
            final FacetHolder holder) {
        super(holder, Precedence.INFERRED);
        this.choicesFacet = choicesFacet;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return choicesFacet.getChoices(adapter, interactionInitiatedBy);
    }

}

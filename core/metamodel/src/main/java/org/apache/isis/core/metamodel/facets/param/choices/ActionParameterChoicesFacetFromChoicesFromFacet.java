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
package org.apache.isis.core.metamodel.facets.param.choices;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.action.choicesfrom.ChoicesFromFacet;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.val;

public class ActionParameterChoicesFacetFromChoicesFromFacet
extends ActionParameterChoicesFacetAbstract {

    public static Optional<ActionParameterChoicesFacet> create(
            final Optional<ChoicesFromFacet> choicesFromFacetIfAny,
            final ObjectSpecification actionOwnerSpec,
            final FacetHolder facetHolder) {
        return choicesFromFacetIfAny
                .map(ChoicesFromFacet::value)
                .flatMap(actionOwnerSpec::getCollection)
                .map(coll->
                    new ActionParameterChoicesFacetFromChoicesFromFacet(coll, facetHolder));
    }

    private final OneToManyAssociation choicesFromCollection;

    private ActionParameterChoicesFacetFromChoicesFromFacet(
            final OneToManyAssociation choicesFromCollection,
            final FacetHolder holder) {
        super(holder, Precedence.LOW); // precedence low, so is overridden by imperative facets (member support)
        this.choicesFromCollection = choicesFromCollection;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ObjectSpecification requiredSpec,
            final ActionInteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val collectionAsObject = choicesFromCollection.get(head.getOwner(), interactionInitiatedBy);
        return CollectionFacet.streamAdapters(collectionAsObject).collect(Can.toCan());
    }

}

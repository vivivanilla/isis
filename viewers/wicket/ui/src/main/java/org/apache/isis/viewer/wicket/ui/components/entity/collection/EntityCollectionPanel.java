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
package org.apache.isis.viewer.wicket.ui.components.entity.collection;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionPresentationSelectorPanel;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityCollectionPanel
extends PanelAbstract<ManagedObject, EntityModel>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_COLLECTION_GROUP = "collectionGroup";
    private static final String ID_COLLECTION_NAME = "collectionName";
    private static final String ID_COLLECTION = "collection";

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final ComponentHintKey selectedItemHintKey;

    @Getter(onMethod_= {@Override})
    private boolean visible = false;

    @Getter(value = AccessLevel.PROTECTED)
    private CollectionPresentationSelectorPanel selectorDropdownPanel;

    private final WebMarkupContainer div;

    public EntityCollectionPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);

        this.div = new WebMarkupContainer(ID_COLLECTION_GROUP);

        selectedItemHintKey = ComponentHintKey.create(super.getCommonContext(),
                this::getSelectorDropdownPanel,
                EntityCollectionModelParented.HINT_KEY_SELECTED_ITEM);

        buildGui();
    }


    /**
     * Attach UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();

        final WebMarkupContainer panel = this;
        if(visible) {
            panel.add(div);
            this.setOutputMarkupId(true);
        } else {
            WktComponents.permanentlyHide(panel, div.getId());
        }

    }

    private void buildGui() {

        val collectionModel = EntityCollectionModelParented.forParentObjectModel(getModel());
        div.setMarkupId("collection-" + collectionModel.getLayoutData().getId());

        val collectionMetaModel = collectionModel.getMetaModel();

        Wkt.cssAppend(div, collectionModel.getIdentifier());
        Wkt.cssAppend(div, collectionModel.getElementType().getFeatureIdentifier());

        val objectAdapter = getModel().getObject();
        final Consent visibility = collectionMetaModel
                .isVisible(objectAdapter, InteractionInitiatedBy.USER, Where.OBJECT_FORMS);

        if(visibility.isAllowed()) {

            visible = true;

            Facets.cssClass(collectionMetaModel, objectAdapter)
            .ifPresent(cssClass->Wkt.cssAppend(div, cssClass));

            val collectionPanel = new CollectionPanel(ID_COLLECTION, collectionModel);
            div.addOrReplace(collectionPanel);

            val labelComponent = Wkt.label(ID_COLLECTION_NAME,
                    collectionMetaModel.getFriendlyName(collectionModel::getParentObject));
            labelComponent.setEscapeModelStrings(true);
            div.add(labelComponent);

            collectionMetaModel.getDescription(collectionModel::getParentObject)
            .ifPresent(description->WktTooltips.addTooltip(labelComponent, description));

            final Can<LinkAndLabel> links = collectionModel.getLinks();
            AdditionalLinksPanel.addAdditionalLinks(
                    div, ID_ADDITIONAL_LINKS, links, AdditionalLinksPanel.Style.INLINE_LIST);

            createSelectorDropdownPanel(collectionModel);
            collectionPanel.setSelectorDropdownPanel(selectorDropdownPanel);

        }
    }

    private void createSelectorDropdownPanel(final EntityCollectionModel collectionModel) {

        final CollectionPresentationSelectorHelper selectorHelper =
                new CollectionPresentationSelectorHelper(collectionModel, getComponentFactoryRegistry(),
                        selectedItemHintKey);

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_SELECTOR_DROPDOWN);
        } else {
            selectorDropdownPanel = new CollectionPresentationSelectorPanel(ID_SELECTOR_DROPDOWN,
                    collectionModel, selectedItemHintKey);
            div.addOrReplace(selectorDropdownPanel);
        }
    }

}

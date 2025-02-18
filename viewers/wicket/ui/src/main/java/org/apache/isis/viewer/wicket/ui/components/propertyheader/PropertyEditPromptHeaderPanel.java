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
package org.apache.isis.viewer.wicket.ui.components.propertyheader;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class PropertyEditPromptHeaderPanel
extends PanelAbstract<ManagedObject, ScalarPropertyModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_PROPERTY_NAME = "propertyName";

    public PropertyEditPromptHeaderPanel(final String id, final ScalarPropertyModel model) {
        super(id, model);

        val targetAdapter = model.getParentUiModel().getManagedObject();

        getComponentFactoryRegistry().addOrReplaceComponent(
                this,
                ComponentType.ENTITY_ICON_AND_TITLE,
                EntityModel.ofAdapter(model.getCommonContext(), targetAdapter));

        Wkt.labelAdd(this, ID_PROPERTY_NAME, model::getFriendlyName)
            .setEscapeModelStrings(true);
    }

}

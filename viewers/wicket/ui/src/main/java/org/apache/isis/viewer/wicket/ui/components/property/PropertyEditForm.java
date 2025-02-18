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
package org.apache.isis.viewer.wicket.ui.components.property;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.commons.functional.Either;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

class PropertyEditForm extends PromptFormAbstract<ScalarPropertyModel> {

    private static final long serialVersionUID = 1L;

    public PropertyEditForm(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final ScalarPropertyModel propertyModel) {
        super(id, parentPanel, settings, propertyModel);
    }

    private ScalarPropertyModel scalarPropertyModel() {
        return (ScalarPropertyModel) super.getModel();
    }

    @Override
    protected void addParameters() {
        val scalarModel = scalarPropertyModel();
        val container = Wkt.containerAdd(this, PropertyEditFormPanel.ID_PROPERTY);

        val component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);

        _Casts.castTo(ScalarPanelAbstract.class, component)
        .ifPresent(scalarModelSubscriber->
            scalarModelSubscriber.notifyOnChange(this)); // handling onUpdate and onError
    }

    @Override
    public void onUpdate(
            final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
    }

    @Override
    protected Either<ActionModel, ScalarPropertyModel> getMemberModel() {
        return Either.right(scalarPropertyModel());
    }

}

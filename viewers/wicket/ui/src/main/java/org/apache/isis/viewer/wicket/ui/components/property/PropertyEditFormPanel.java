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

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersFormPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormPanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} to capture the arguments for an action
 * invocation.
 * <p>
 * Corresponding panel for property edits is {@link ActionParametersFormPanel}.
 */
public class PropertyEditFormPanel
extends PromptFormPanelAbstract<ManagedObject, ScalarPropertyModel> {

    private static final long serialVersionUID = 1L;

    static final String ID_PROPERTY = "property";
    static final String ID_INPUT_FORM = "inputForm";

    public PropertyEditFormPanel(final String id, final ScalarPropertyModel model) {
        super(id, model);
        buildGui();
    }

    private void buildGui() {
        val scalarPropertyModel = scalarPropertyModel();
        add(new PropertyEditForm(ID_INPUT_FORM, this, this.getWicketViewerSettings(), scalarPropertyModel));
    }

    private ScalarPropertyModel scalarPropertyModel() {
        return getModel();
    }

}

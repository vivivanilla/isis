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
package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.wicketstuff.select2.Select2MultiChoice;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;

class EntityLinkSelect2Panel
extends FormComponentPanelAbstract<ManagedObject>
implements CancelHintRequired  {

    private static final long serialVersionUID = 1L;

    private ReferencePanel owningPanel;

    public EntityLinkSelect2Panel(final String id, final ReferencePanel owningPanel) {
        super(id, owningPanel.scalarModel());
        this.owningPanel = owningPanel;
        setType(ManagedObject.class);
    }

    @Override
    public void validate() {
        if(shouldValidate()) {
            super.validate();
        }
    }

    /**
     * Necessary because {@link FormComponentPanel} overrides this as <tt>true</tt>, whereas we want to
     * report on the state of the underlying {@link org.wicketstuff.select2.Select2Choice} or
     * {@link Select2MultiChoice}.
     */
    @Override
    public boolean checkRequired() {
        return owningPanel.getSelect2().checkRequired();
    }

    /**
     * Since we override {@link #convertInput()}, it is (apparently) enough to
     * just return a value that is suitable for error reporting.
     */
    @Override
    public String getInput() {
        return owningPanel.getTitleForFormComponentInput();
    }

    @Override
    public void convertInput() {
        owningPanel.convertInput();
    }

    @Override
    public void onCancel() {
        // no-op
    }

    // -- HELPER

    /**
     * [ISIS-3070]
     * Skip validation if one of the callers is an {@link AjaxFormComponentUpdatingBehavior},
     * as this validation might be called too early,
     * when the pending value is not yet updated to the user's (new) input.
     * Instead wait for the AjaxFormComponentUpdatingBehavior, which happens later,
     * when the actual pending value is available for validation.
     * <p>
     * This is a hack, for the lack of a better solution yet.
     */
    private boolean shouldValidate() {
        return !_Exceptions
        .streamStackTrace()
        .anyMatch(element->
                    element.getClassName().contains(AjaxFormComponentUpdatingBehavior.class.getSimpleName()));
    }

}

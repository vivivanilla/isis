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
package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import java.util.EnumSet;

import org.apache.wicket.Component;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithValueSemantics;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

/**
 * Panel for rendering titles for scalars of any type.
 * <p>
 * Most prominently this is used for {@link Enum} values.
 */
public class ScalarTitleBadgePanel<T> extends ScalarPanelTextFieldWithValueSemantics<T> {

    private static final long serialVersionUID = 1L;

    public ScalarTitleBadgePanel(final String id, final ScalarModel scalarModel, final Class<T> type) {
        super(id, scalarModel, type);
    }

    @Override
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {
        modifiers.add(FormatModifier.BADGE);
    }

    @Override
    protected Component createComponentForOutput(final String id) {
        return CompactFragment.BADGE.createFragment(id, this, scalarValueId->
            Wkt.labelWithDynamicEscaping(scalarValueId, this::obtainOutputFormat));
    }

}

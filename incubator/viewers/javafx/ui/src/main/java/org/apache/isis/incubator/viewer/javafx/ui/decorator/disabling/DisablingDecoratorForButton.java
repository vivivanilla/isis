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
package org.apache.isis.incubator.viewer.javafx.ui.decorator.disabling;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.viewer.common.model.decorators.DisablingDecorator;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DisablingDecoratorForButton implements DisablingDecorator<Button> {

    @Override
    public void decorate(final Button uiButton, final DisablingDecorationModel disableUiModel) {

        val reason = disableUiModel.getReason();

        uiButton.setTooltip(new Tooltip(reason));
        uiButton.disableProperty().set(true);
        uiButton.setOnAction(null);

        //uiComponent.getStyleClass().add("button-disabled");

    }

}

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
package demoapp.dom.domain.actions.progmodel.assoc;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.AssociatedAction")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
@NoArgsConstructor
public class AssociatedActionDemo implements HasAsciiDocDescription {

    public static AssociatedActionDemo createWithDemoData() {
        val demo = new AssociatedActionDemo();
        demo.getItems().clear();
        demo.getItems().add(DemoItem.of("first"));
        demo.getItems().add(DemoItem.of("second"));
        demo.getItems().add(DemoItem.of("third"));
        demo.getItems().add(DemoItem.of("last"));
        return demo;
    }

    @XmlTransient
    @Inject MessageService messageService;

    @Getter private final Set<DemoItem> items = new LinkedHashSet<>();

    @ObjectSupport
    public String title() {
        return "Associated Action Demo";
    }

    @Action(choicesFrom = "items")
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public AssociatedActionDemo doSomethingWithItems(

            // bulk selection
            final Set<DemoItem> items) {

        if(items!=null) {
            items.forEach(item->messageService.informUser(item.getName()));
        }
        return this;
    }


}

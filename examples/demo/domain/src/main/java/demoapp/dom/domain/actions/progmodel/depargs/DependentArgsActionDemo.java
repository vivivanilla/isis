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
package demoapp.dom.domain.actions.progmodel.depargs;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.value.Markup;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.DependentArgs")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.ENABLED)
public class DependentArgsActionDemo implements HasAsciiDocDescription {

    public String title() {
        return "Dependent Arguments Demo";
    }

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(describedAs = "Default for first dialog paramater in 'Choices', 'Auto Complete' and 'Default'")
    @Getter @Setter
    private Parity dialogParityDefault = null;

    @Property
    @PropertyLayout(describedAs = "Default for first dialog paramater in 'Hide' and 'Disable'")
    @Getter @Setter
    private boolean dialogCheckboxDefault = false;

    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getDependentText1() {
        return new Markup("Click one of above actions to see how dependent arguments work. "
                + "Set defaults for the first dialog parameter here:");
    }

    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getDependentText2() {
        return new Markup("Click one of above actions to see how dependent arguments work. "
                + "Set defaults for the first dialog parameter here:");
    }

    @Property
    @PropertyLayout(labelPosition=LabelPosition.NONE)
    public Markup getIndependentText() {
        return new Markup("Click this action above to see independent arguments do not clear "
                + "each other when changing.");
    }

    @Collection
    @Getter
    private final Set<DemoItem> items = new LinkedHashSet<>();

}


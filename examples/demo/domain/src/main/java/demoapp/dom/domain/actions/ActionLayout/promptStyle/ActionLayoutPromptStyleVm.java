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
package demoapp.dom.domain.actions.ActionLayout.promptStyle;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@Named("demo.ActionLayoutPromptStyleVm")
@DomainObject(
        nature=Nature.VIEW_MODEL)
public class ActionLayoutPromptStyleVm implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "ActionLayout#promptStyle";
    }

    @Property(editing = Editing.DISABLED)
    @PropertyLayout(fieldSetId = "general", sequence = "1")
    @XmlElement(required = true)
    @Getter @Setter
    private String title;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "annotated", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String readOnlyProperty1;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            describedAs = "has associated action with promptStyle=INLINE_AS_IF_EDIT",
            fieldSetId = "annotated",
            sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String readOnlyProperty2;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "layout", sequence = "1")
    @XmlElement(required = false)
    @Getter @Setter
    private String readOnlyProperty3;

    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(
            describedAs = "has associated action with promptStyle=INLINE_AS_IF_EDIT",
            fieldSetId = "layout",
            sequence = "2")
    @XmlElement(required = false)
    @Getter @Setter
    private String readOnlyProperty4;

}
//end::class[]

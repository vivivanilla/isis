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
package org.apache.isis.core.metamodel.inspect.model;

import java.util.stream.Stream;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Introspection;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.schema.metamodel.v2.Action;
import org.apache.isis.schema.metamodel.v2.Member;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Named(ActionNode.LOGICAL_TYPE_NAME)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        introspection = Introspection.ANNOTATION_REQUIRED
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
public class ActionNode extends MemberNode {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".ActionNode";

    @Property(hidden = Where.EVERYWHERE)
    @Getter @Setter private Action action;

    @Override
    public String createTitle() {
        return String.format("%s(...): %s%s",
                action.getId(),
                typeToString(action.getReturnType()),
                titleSuffix());
    }

    @Override
    public Stream<MMNode> streamChildNodes() {

        return Stream.<MMNode>concat(

                super.streamChildNodes(),

                action.getParams().getParam().stream()
                    .map(param->MMNodeFactory.param(param, this))

                );
    }

    @Override
    protected Member member() {
        return action;
    }

}


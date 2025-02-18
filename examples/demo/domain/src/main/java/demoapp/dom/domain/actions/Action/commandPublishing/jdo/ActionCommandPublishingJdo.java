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
package demoapp.dom.domain.actions.Action.commandPublishing.jdo;

import javax.inject.Named;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.springframework.context.annotation.Profile;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingDisabledMetaAnnotation;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingEnabledMetaAnnotation;
import demoapp.dom.domain.actions.Action.commandPublishing.ActionCommandPublishingEntity;

@Profile("demo-jdo")
//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@Named("demo.ActionCommandPublishingEntity")
@DomainObject(
        nature=Nature.ENTITY
        , editing = Editing.DISABLED
)
public class ActionCommandPublishingJdo
        extends ActionCommandPublishingEntity {
    // ...
//end::class[]

    public ActionCommandPublishingJdo(final String initialValue) {
        this.property = initialValue;
        this.propertyCommandDisabled = initialValue;
        this.propertyMetaAnnotated = initialValue;
        this.propertyMetaAnnotatedOverridden = initialValue;
    }

    @ObjectSupport public String title() {
        return "Action#commandPublishing (JDO)";
    }

//tag::property[]
    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "1")
    @Getter @Setter
    private String property;

    @Property()
    @PropertyLayout(fieldSetId = "annotation", sequence = "2")
    @Getter @Setter
    private String propertyCommandDisabled;

    @Property()
    @PropertyLayout(fieldSetId = "meta-annotated", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotated;

    @Property()
    @PropertyLayout(fieldSetId = "meta-annotated-overridden", sequence = "1")
    @Getter @Setter
    private String propertyMetaAnnotatedOverridden;
//end::property[]

//tag::annotation[]
    @Action(
        commandPublishing = Publishing.ENABLED                  // <.>
        , semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(command = ENABLED)"
        , associateWith = "property"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingAnnotation(final String value) {
        // ...
//end::annotation[]
        setProperty(value);
        return this;
    }
    @MemberSupport public String default0UpdatePropertyUsingAnnotation() {
        return getProperty();
//tag::annotation[]
    }
//end::annotation[]

//tag::annotation-2[]
    @Action(
        commandPublishing = Publishing.DISABLED                 // <.>
        , semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@Action(command = ENABLED)"
        , associateWith = "propertyCommandDisabled"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyCommandDisabledUsingAnnotation(final String value) {
        // ...
//end::annotation-2[]
        setPropertyCommandDisabled(value);
        return this;
    }
    @MemberSupport public String default0UpdatePropertyCommandDisabledUsingAnnotation() {
        return getPropertyCommandDisabled();
//tag::annotation-2[]
    }
//end::annotation-2[]

//tag::meta-annotation[]
    @ActionCommandPublishingEnabledMetaAnnotation                 // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs = "@ActionCommandEnabledMetaAnnotation"
        , associateWith = "propertyMetaAnnotated"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingMetaAnnotation(final String value) {
        // ...
//end::meta-annotation[]
        setPropertyMetaAnnotated(value);
        return this;
    }
    @MemberSupport public String default0UpdatePropertyUsingMetaAnnotation() {
        return getPropertyMetaAnnotated();
//tag::meta-annotation[]
    }
//end::meta-annotation[]

//tag::meta-annotation-overridden[]
    @ActionCommandPublishingDisabledMetaAnnotation                // <.>
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , commandPublishing = Publishing.ENABLED                  // <.>
    )
    @ActionLayout(
        named = "Update Property"
        , describedAs =
            "@ActionCommandDisabledMetaAnnotation @Action(command = ENABLED)"
        , associateWith = "propertyMetaAnnotatedOverridden"
        , sequence = "1"
    )
    public ActionCommandPublishingJdo updatePropertyUsingMetaAnnotationButOverridden(final String value) {
        // ...
//end::meta-annotation-overridden[]
        setPropertyMetaAnnotatedOverridden(value);
        return this;
    }
    @MemberSupport public String default0UpdatePropertyUsingMetaAnnotationButOverridden() {
        return getPropertyMetaAnnotatedOverridden();
//tag::meta-annotation-overridden[]
    }
//end::meta-annotation-overridden[]


//tag::class[]

}
//end::class[]

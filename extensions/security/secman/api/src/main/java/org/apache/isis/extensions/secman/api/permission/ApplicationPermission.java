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
package org.apache.isis.extensions.secman.api.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

/**
 * Specifies how a particular {@link #getRole() application role} may interact with a specific
 * {@link ApplicationFeature application feature}.
 *
 * <p>
 *     Each permission has a {@link #getRule() rule} and a {@link #getMode() mode}.  The
 *     {@link ApplicationPermissionRule rule} determines whether the permission
 *     {@link ApplicationPermissionRule#ALLOW grants}
 *     access to the feature or {@link ApplicationPermissionRule#VETO veto}es access
 *     to it.  The {@link ApplicationPermissionMode mode} indicates whether
 *     the role can {@link ApplicationPermissionMode#VIEWING view} the feature
 *     or can {@link ApplicationPermissionMode#CHANGING change} the state of the
 *     system using the feature.
 * </p>
 *
 * <p>
 *     For a given permission, there is an interaction between the {@link ApplicationPermissionRule rule} and the
 *     {@link ApplicationPermissionMode mode}:
 * <ul>
 *     <li>for an {@link ApplicationPermissionRule#ALLOW allow}, a
 *     {@link ApplicationPermissionMode#CHANGING usability} allow
 *     implies {@link ApplicationPermissionMode#VIEWING visibility} allow.
 *     </li>
 *     <li>conversely, for a {@link ApplicationPermissionRule#VETO veto},
 *     a {@link ApplicationPermissionMode#VIEWING visibility} veto
 *     implies a {@link ApplicationPermissionMode#CHANGING usability} veto.</li>
 * </ul>
 * </p>
 *
 * @since 2.0 {@index}
 */
@DomainObject(
        objectType = "isis.ext.secman.IApplicationPermission"
)
@DomainObjectLayout(
        titleUiEvent = ApplicationPermission.TitleUiEvent.class,
        iconUiEvent = ApplicationPermission.IconUiEvent.class,
        cssClassUiEvent = ApplicationPermission.CssClassUiEvent.class,
        layoutUiEvent = ApplicationPermission.LayoutUiEvent.class
)
public interface ApplicationPermission extends Comparable<ApplicationPermission>{


    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationPermission, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationPermission, T> {}


    // -- UI EVENTS

    class TitleUiEvent extends IsisModuleExtSecmanApi.TitleUiEvent<ApplicationPermission> {}
    class IconUiEvent extends IsisModuleExtSecmanApi.IconUiEvent<ApplicationPermission> {}
    class CssClassUiEvent extends IsisModuleExtSecmanApi.CssClassUiEvent<ApplicationPermission> {}
    class LayoutUiEvent extends IsisModuleExtSecmanApi.LayoutUiEvent<ApplicationPermission> {}



    // -- ROLE

    @Property(
            domainEvent = Role.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden=Where.REFERENCES_PARENT,
            fieldSetId="identity",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Role {
        class DomainEvent extends PropertyDomainEvent<ApplicationRole> {}
    }

    @Role
    ApplicationRole getRole();
    void setRole(ApplicationRole applicationRole);


    // -- FEATURE FQN

    @Property(
            domainEvent = FeatureFqn.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = FeatureFqn.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="identity",
            sequence = "2"
    )
    @Parameter(
            maxLength = FeatureFqn.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface FeatureFqn {
        int MAX_LENGTH = 1024;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FeatureFqn
    String getFeatureFqn();
    void setFeatureFqn(String featureFqn);


    // -- FEATURE SORT
    /**
     * The {@link ApplicationFeatureId#getSort() feature sort} of the
     * feature.
     *
     * <p>
     *     The combination of the feature sort and the
     *     {@link #getFeatureFqn() fully qualified name} is used to build
     *     the corresponding feature (view model).
     * </p>
     *
     * @see #getFeatureFqn()
     */
    @Programmatic
    ApplicationFeatureSort getFeatureSort();


    // -- SORT

    /**
     * Whether this feature is a namespace, type or member.
     */
    @Property(
            domainEvent = Sort.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Sort.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="feature",
            sequence = "1"
    )
    @Parameter(
            maxLength = Sort.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sort {
        int MAX_LENGTH = 7;  // ApplicationFeatureType.PACKAGE is longest

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Sort
    String getSort();




    // -- RULE

    @Property(
            domainEvent = Rule.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="permissions",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Rule {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermissionRule> {}
    }

    @Rule
    ApplicationPermissionRule getRule();
    void setRule(ApplicationPermissionRule rule);


    // -- MODE

    @Property(
            domainEvent = Mode.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="permissions",
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Mode {
        class DomainEvent extends PropertyDomainEvent<ApplicationPermissionMode> {}
    }

    @Mode
    ApplicationPermissionMode getMode();
    void setMode(ApplicationPermissionMode mode);




    // -- HELPER

    @Programmatic
    default Optional<ApplicationFeatureId> asFeatureId() {
        return Optional.ofNullable(getFeatureSort())
                .map(featureSort -> ApplicationFeatureId.newFeature(featureSort, getFeatureFqn()));
    }


}

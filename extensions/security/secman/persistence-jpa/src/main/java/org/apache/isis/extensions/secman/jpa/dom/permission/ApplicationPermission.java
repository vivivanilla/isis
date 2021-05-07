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
package org.apache.isis.extensions.secman.jpa.dom.permission;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jpa.dom.constants.NamedQueryNames;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@SuppressWarnings("JpaQlInspection")
@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationPermission",
        uniqueConstraints=
            @UniqueConstraint(
                    name = "ApplicationPermission_role_feature_rule_UNQ",
                    columnNames={"roleId", "featureSort", "featureFqn", "rule"})
)
@NamedQueries({
    @NamedQuery(
            name = NamedQueryNames.PERMISSION_BY_ROLE,
            query = "SELECT p "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission p "
                  + "WHERE p.role = :role"),
    @NamedQuery(
            name = NamedQueryNames.PERMISSION_BY_USER,
            //TODO this query returns empty result
            query = "SELECT p "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission p "
                  + ", org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser u "
                  + "WHERE u.username = :username"
                  + "    AND p.role MEMBER OF u.roles"),
    @NamedQuery(
            name = NamedQueryNames.PERMISSION_BY_FEATURE,
            query = "SELECT p "
                    + "FROM org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission p "
                    + "WHERE p.featureSort = :featureSort "
                    + "   AND p.featureFqn = :featureFqn"),
    @NamedQuery(
            name = NamedQueryNames.PERMISSION_BY_ROLE_RULE_FEATURE_FQN,
            query = "SELECT p "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission p "
                  + "WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "
                  + "   AND p.featureFqn = :featureFqn "),
    @NamedQuery(
            name = NamedQueryNames.PERMISSION_BY_ROLE_RULE_FEATURE,
            query = "SELECT p "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission p "
                  + "WHERE p.role = :role "
                  + "   AND p.rule = :rule "
                  + "   AND p.featureSort = :featureSort "),
})
@EntityListeners(JpaEntityInjectionPointResolver.class)
@DomainObject(
        objectType = "isis.ext.secman.ApplicationPermission"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
implements
    org.apache.isis.extensions.secman.api.permission.ApplicationPermission {

    @Inject transient ApplicationFeatureRepository featureRepository;


    @javax.persistence.Id
    @javax.persistence.GeneratedValue
    private Long id;

    @javax.persistence.Version
    private Long version;


    // -- ROLE

    @Role
    @SuppressWarnings("JpaAttributeTypeInspection")
    @JoinColumn(name="roleId", nullable=false)
    @Getter(onMethod = @__(@Override))
    private ApplicationRole role;

    public void setRole(ApplicationRole role) {
        this.role = role;
    }

    @Override
    public void setRole(org.apache.isis.extensions.secman.api.role.ApplicationRole applicationRole) {
        setRole((ApplicationRole) applicationRole);
    }


    // -- FEATURE FQN

    @FeatureFqn
    @Column(nullable=false, length = FeatureFqn.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String featureFqn;


    // -- FEATURE SORT

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter(onMethod = @__({@Override, @Programmatic}))
    @Setter
    private ApplicationFeatureSort featureSort;


    // -- SORT

    @Sort
    @Override
    public String getSort() {
        final Enum<?> e = getFeatureSort() != ApplicationFeatureSort.MEMBER
                ? getFeatureSort()
                : getMemberSort().orElse(null);
        return e != null ? e.name(): null;
    }

    @Programmatic
    private Optional<ApplicationMemberSort> getMemberSort() {
        return getFeature()
                .flatMap(ApplicationFeature::getMemberSort);
    }

    private Optional<ApplicationFeature> getFeature() {
        return asFeatureId()
                .map(featureId -> featureRepository.findFeature(featureId));
    }


    // -- RULE

    @Rule
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ApplicationPermissionRule rule;


    // -- MODE

    @Mode
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ApplicationPermissionMode mode;



    // -- CONTRACT

    private static final ObjectContract<ApplicationPermission> contract	=
            ObjectContracts.contract(ApplicationPermission.class)
            .thenUse("role", ApplicationPermission::getRole)
            .thenUse("featureSort", ApplicationPermission::getFeatureSort)
            .thenUse("featureFqn", ApplicationPermission::getFeatureFqn)
            .thenUse("mode", ApplicationPermission::getMode);

    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.permission.ApplicationPermission other) {
        return contract.compare(this, (ApplicationPermission) other);
    }

    @Override
    public boolean equals(final Object other) {
        return contract.equals(this, other);
    }

    @Override
    public int hashCode() {
        return contract.hashCode(this);
    }

    @Override
    public String toString() {
        return contract.toString(this);
    }

    // --

    public static class DefaultComparator implements Comparator<ApplicationPermission> {
        @Override
        public int compare(final ApplicationPermission o1, final ApplicationPermission o2) {
            return Objects.compare(o1, o2, ApplicationPermission::compareTo);
        }
    }


    // -- Functions

    @UtilityClass
    public static final class Functions {

        public static final Function<ApplicationPermission, ApplicationPermissionValue> AS_VALUE =
                (ApplicationPermission input) ->
                    new ApplicationPermissionValue(
                            input.asFeatureId().orElseThrow(_Exceptions::noSuchElement),
                            input.getRule(),
                            input.getMode());

    }


}

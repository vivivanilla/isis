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
package org.apache.isis.extensions.secman.jdo.dom.permission;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValue;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationPermission")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByRole", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role"),
    @javax.jdo.annotations.Query(
            name = "findByUser", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE (u.roles.contains(role) && u.username == :username) "
                    + "VARIABLES org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser u"),
    @javax.jdo.annotations.Query(
            name = "findByFeature", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE featureSort == :featureSort "
                    + "   && featureFqn == :featureFqn"),
    @javax.jdo.annotations.Query(
            name = "findByRoleAndRuleAndFeature", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureSort == :featureSort "
                    + "   && featureFqn == :featureFqn "),
    @javax.jdo.annotations.Query(
            name = "findByRoleAndRuleAndFeatureType", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission "
                    + "WHERE role == :role "
                    + "   && rule == :rule "
                    + "   && featureSort == :featureSort "),
})
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationPermission_role_feature_rule_UNQ",
            members = { "role", "featureSort", "featureFqn", "rule" })
})
@DomainObject(
        objectType = "isis.ext.secman.ApplicationPermission"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_CHILD
)
public class ApplicationPermission
implements org.apache.isis.extensions.secman.api.permission.ApplicationPermission  {

    @Inject ApplicationFeatureRepository featureRepository;


    // -- ROLE

    @Role
    @javax.jdo.annotations.Column(name = "roleId", allowsNull="false")
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
    @javax.jdo.annotations.Column(allowsNull="false", length = FeatureFqn.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String featureFqn;


    // -- FEATURE SORT

    @javax.jdo.annotations.Column(allowsNull="false")
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
    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ApplicationPermissionRule rule;


    // -- MODE

    @Mode
    @javax.jdo.annotations.Column(allowsNull="false")
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

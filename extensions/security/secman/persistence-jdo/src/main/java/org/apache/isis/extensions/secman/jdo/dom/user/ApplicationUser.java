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
package org.apache.isis.extensions.secman.jdo.dom.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.VersionStrategy;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isisExtensionsSecman",
        table = "ApplicationUser")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@javax.jdo.annotations.Uniques({
    @javax.jdo.annotations.Unique(
            name = "ApplicationUser_username_UNQ", members = { "username" })
})
@javax.jdo.annotations.Queries( {
    @javax.jdo.annotations.Query(
            name = "findByUsername", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username == :username"),
    @javax.jdo.annotations.Query(
            name = "findByEmailAddress", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE emailAddress == :emailAddress"),
    @javax.jdo.annotations.Query(
            name = "findByAtPath", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE atPath == :atPath"),
    @javax.jdo.annotations.Query(
            name = "findByName", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username.matches(:nameRegex)"
                    + "   || familyName.matches(:nameRegex)"
                    + "   || givenName.matches(:nameRegex)"
                    + "   || knownAs.matches(:nameRegex)"),
    @javax.jdo.annotations.Query(
            name = "find", language = "JDOQL",
            value = "SELECT "
                    + "FROM org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser "
                    + "WHERE username.matches(:regex)"
                    + " || familyName.matches(:regex)"
                    + " || givenName.matches(:regex)"
                    + " || knownAs.matches(:regex)"
                    + " || emailAddress.matches(:regex)")
})
@DomainObject(
        objectType = "isis.ext.secman.ApplicationUser",
        autoCompleteRepository = ApplicationUserRepository.class,
        autoCompleteAction = "findMatching"
        )
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationUser
        implements org.apache.isis.extensions.secman.api.user.ApplicationUser {

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private UserService userService;
    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default.
     * implementation.
     */
    @Inject private PermissionsEvaluationService permissionsEvaluationService;
    @Inject private SecmanConfiguration configBean;



    @Username
    @javax.jdo.annotations.Column(allowsNull="false", length = Username.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String username;


    @AccountType
    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private org.apache.isis.extensions.secman.api.user.AccountType accountType;


    @Status
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ApplicationUserStatus status;


    @AtPath
    @javax.jdo.annotations.Column(name = "atPath", allowsNull="true", length = AtPath.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String atPath;


    @FamilyName
    @javax.jdo.annotations.Column(allowsNull="true", length = FamilyName.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String familyName;


    @GivenName
    @javax.jdo.annotations.Column(allowsNull="true", length = GivenName.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String givenName;


    @KnownAs
    @javax.jdo.annotations.Column(allowsNull="true", length = KnownAs.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String knownAs;


    @EmailAddress
    @javax.jdo.annotations.Column(allowsNull="true", length = EmailAddress.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String emailAddress;


    @PhoneNumber
    @javax.jdo.annotations.Column(allowsNull="true", length = PhoneNumber.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String phoneNumber;


    @FaxNumber
    @javax.jdo.annotations.Column(allowsNull="true", length = FaxNumber.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String faxNumber;


    @EncryptedPassword
    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String encryptedPassword;


    @HasPassword
    @Override
    public boolean isHasPassword() {
        return org.apache.isis.extensions.secman.api.user.ApplicationUser.super.isHasPassword();
    }


    @Roles
    @javax.jdo.annotations.Persistent(table="ApplicationUserRoles")
    @javax.jdo.annotations.Join(column="userId")
    @javax.jdo.annotations.Element(column="roleId")
    @Getter @Setter
    private Set<ApplicationRole> roles = new TreeSet<>();


    // -- PermissionSet (programmatic)

    // short-term caching
    private transient ApplicationPermissionValueSet cachedPermissionSet;
    @Override
    @Programmatic
    public ApplicationPermissionValueSet getPermissionSet() {
        if(cachedPermissionSet != null) {
            return cachedPermissionSet;
        }
        val permissions = applicationPermissionRepository.findByUser(this);
        return cachedPermissionSet =
                new ApplicationPermissionValueSet(
                        _Lists.map(permissions, ApplicationPermission.Functions.AS_VALUE),
                        permissionsEvaluationService);
    }

    @Override
    public boolean isForSelfOrRunAsAdministrator() {
        return isForSelf() || isRunAsAdministrator();
    }

    // -- helpers
    boolean isForSelf() {
        final String currentUserName = userService.currentUserElseFail().getName();
        return Objects.equals(getUsername(), currentUserName);
    }
    boolean isRunAsAdministrator() {
        final UserMemento currentUser = userService.currentUserElseFail();
        final List<RoleMemento> roles = currentUser.getRoles();

        val adminRoleSuffix = ":" + configBean.getAdminRoleName();

        for (final RoleMemento role : roles) {
            final String roleName = role.getName();
            // format is realmName:roleName.
            // since we don't know what the realm's name is (depends on its configuration in shiro.ini),
            // simply check that the last part matches the role name.
            if(roleName.endsWith(adminRoleSuffix)) {
                return true;
            }
        }
        return false;
    }


    // -- equals, hashCode, compareTo, toString
    private static final String propertyNames = "username";

    private static final ObjectContract<ApplicationUser> contract =
            ObjectContracts.parse(ApplicationUser.class, propertyNames);


    @Override
    public int compareTo(final org.apache.isis.extensions.secman.api.user.ApplicationUser o) {
        return contract.compare(this, (ApplicationUser) o);
    }

    @Override
    public boolean equals(final Object obj) {
        return contract.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return contract.hashCode(this);
    }

    @Override
    public String toString() {
        return contract.toString(this);
    }

}

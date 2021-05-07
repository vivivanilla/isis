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
package org.apache.isis.extensions.secman.jpa.dom.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
import org.apache.isis.extensions.secman.jpa.dom.constants.NamedQueryNames;
import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jpa.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jpa.dom.role.ApplicationRole;
import org.apache.isis.persistence.jpa.applib.integration.JpaEntityInjectionPointResolver;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@SuppressWarnings("JpaQlInspection")
@Entity
@Table(
        schema = "isisExtensionsSecman",
        name = "ApplicationUser",
        uniqueConstraints =
            @UniqueConstraint(
                    name = "ApplicationUser_username_UNQ",
                    columnNames={"username"})
)
@NamedQueries({
    @NamedQuery(
            name = NamedQueryNames.USER_BY_USERNAME,
            query = "SELECT u "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser u "
                  + "WHERE u.username = :username"),
    @NamedQuery(
            name = NamedQueryNames.USER_BY_EMAIL,
            query = "SELECT u "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser u "
                  + "WHERE u.emailAddress = :emailAddress"),
    @NamedQuery(
            name = NamedQueryNames.USER_BY_ATPATH,
            query = "SELECT u "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser u "
                  + "WHERE u.atPath = :atPath"),
    @NamedQuery(
            name = NamedQueryNames.USER_FIND,
            query = "SELECT u "
                  + "FROM org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser u "
                  + "WHERE u.username LIKE :regex"
                  + "  OR u.familyName LIKE :regex"
                  + "  OR u.givenName LIKE :regex"
                  + "  OR u.knownAs LIKE :regex"
                  + "  OR u.emailAddress LIKE :regex")
})
@EntityListeners(JpaEntityInjectionPointResolver.class)
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

    @Inject private transient ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private transient UserService userService;

    /**
     * Optional service, if configured then is used to evaluate permissions within
     * {@link org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet#evaluate(ApplicationFeatureId, ApplicationPermissionMode)}
     * else will fallback to a default.
     * implementation.
     */
    @Inject private transient PermissionsEvaluationService permissionsEvaluationService;
    @Inject private transient SecmanConfiguration configBean;


    @javax.persistence.Id
    @javax.persistence.GeneratedValue
    private Long id;

    @javax.persistence.Version
    private Long version;


    @Username
    @Column(nullable=false, length= Username.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String username;


    @AccountType
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private org.apache.isis.extensions.secman.api.user.AccountType accountType;


    @Status
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ApplicationUserStatus status;


    @AtPath
    @Column(name="atPath", nullable=true, length = AtPath.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String atPath;


    @FamilyName
    @Column(nullable=true, length= FamilyName.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String familyName;


    @GivenName
    @Column(nullable=true, length= GivenName.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String givenName;


    @KnownAs
    @Column(nullable=true, length= KnownAs.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String knownAs;


    @EmailAddress
    @Column(nullable=true, length= EmailAddress.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String emailAddress;


    @PhoneNumber
    @Column(nullable=true, length= PhoneNumber.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String phoneNumber;


    @FaxNumber
    @Column(nullable=true, length= FaxNumber.MAX_LENGTH)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String faxNumber;


    @EncryptedPassword
    @Column(nullable=true)
    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private String encryptedPassword;


    @HasPassword
    @Override
    public boolean isHasPassword() {
        return org.apache.isis.extensions.secman.api.user.ApplicationUser.super.isHasPassword();
    }


    @Roles
    @ManyToMany(mappedBy = "users", cascade = CascadeType.ALL)
    @JoinTable(
            name = "ApplicationUserRoles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
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

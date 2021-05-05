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
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.user.RoleMemento;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ObjectContracts.ObjectContract;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.user.AccountType;
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
public class ApplicationUser implements Comparable<ApplicationUser>,
org.apache.isis.extensions.secman.api.user.ApplicationUser {

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



    // -- username (property)

    @Property(
            domainEvent = UsernameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_USERNAME
    )
    @PropertyLayout(
            fieldSetId="identity",
            sequence = "1"
    )
    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_USERNAME)
    @Getter @Setter
    private String username;


    // -- accountType (property)

    @Property(
            domainEvent = AccountTypeDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "1"
    )
    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter @Setter
    private AccountType accountType;


    // -- status (property)

    @Property(
            domainEvent = StatusDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "2"
    )
    @javax.jdo.annotations.Column(allowsNull="false")
    @Getter @Setter
    private ApplicationUserStatus status;


    // -- atPath (property)

    @Property(
            domainEvent = AtPathDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_AT_PATH
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "3"
    )
    @javax.jdo.annotations.Column(name = "atPath", allowsNull="true", length = MAX_LENGTH_AT_PATH)
    @Getter @Setter
    private String atPath;


    // -- familyName (property)

    @Property(
            domainEvent = FamilyNameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_FAMILY_NAME
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "1"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_FAMILY_NAME)
    @Getter @Setter
    private String familyName;


    // -- givenName (property)

    @Property(
            domainEvent = GivenNameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_KNOWN_AS
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "2"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_GIVEN_NAME)
    @Getter @Setter
    private String givenName;


    // -- knownAs (property)

    @Property(
            domainEvent = KnownAsDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_KNOWN_AS
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "3"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_KNOWN_AS)
    @Getter @Setter
    private String knownAs;


    // -- emailAddress (property)

    @Property(
            domainEvent = EmailAddressDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_EMAIL_ADDRESS
    )
    @PropertyLayout(
            fieldSetId="contactDetails",
            sequence = "1"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_EMAIL_ADDRESS)
    @Getter @Setter
    private String emailAddress;


    // -- phoneNumber (property)

    @Property(
            domainEvent = PhoneNumberDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_PHONE_NUMBER
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES,
            fieldSetId="contactDetails",
            sequence = "2"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_PHONE_NUMBER)
    @Getter @Setter
    private String phoneNumber;


    // -- faxNumber (property)

    @Property(
            domainEvent = FaxNumberDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_PHONE_NUMBER
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="contactDetails",
            sequence = "3"
    )
    @javax.jdo.annotations.Column(allowsNull="true", length = MAX_LENGTH_PHONE_NUMBER)
    @Getter @Setter
    private String faxNumber;


    // -- encryptedPassword (hidden property)

    @PropertyLayout(hidden=Where.EVERYWHERE)
    @javax.jdo.annotations.Column(allowsNull="true")
    @Getter @Setter
    private String encryptedPassword;



    // -- hasPassword (derived property)

    @Property(
            domainEvent = HasPasswordDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="password",
            sequence = "1"
    )
    @Override
    public boolean isHasPassword() {
        return org.apache.isis.extensions.secman.api.user.ApplicationUser.super.isHasPassword();
    }


    // -- roles (collection)

    @Collection(
            domainEvent = RolesDomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20"
    )
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
    public int compareTo(final ApplicationUser o) {
        return contract.compare(this, o);
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

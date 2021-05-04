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
package org.apache.isis.extensions.secman.api.user;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.mixins.security.HasUsername;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionValueSet;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.tenancy.HasAtPath;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
public interface ApplicationUser extends HasUsername, HasAtPath {

    // -- CONSTANTS

    int MAX_LENGTH_USERNAME = 120;
    int MAX_LENGTH_FAMILY_NAME = 120;
    int MAX_LENGTH_GIVEN_NAME = 120;
    int MAX_LENGTH_KNOWN_AS = 120;
    int MAX_LENGTH_EMAIL_ADDRESS = 120;
    int MAX_LENGTH_PHONE_NUMBER = 120;
    int MAX_LENGTH_AT_PATH = 254;

    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}

    // -- MODEL

    default String title() {
        final StringBuilder buf = new StringBuilder();
        if(getFamilyName() != null) {
            if(getKnownAs() != null) {
                buf.append(getKnownAs());
            } else {
                buf.append(getGivenName());
            }
            buf.append(' ')
                    .append(getFamilyName())
                    .append(" (").append(getUsername()).append(')');
        } else {
            buf.append(getUsername());
        }
        return buf.toString();
    }

    default String iconName() {
        return getStatus().isEnabled() ? "enabled" : "disabled";
    }


    // -- username (property)

    class UsernameDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = UsernameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_USERNAME
    )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES,
            fieldSetId="identity",
            sequence = "1")
    @Override
    String getUsername();
    void setUsername(String username);


    // -- familyName (property)

    class FamilyNameDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = FamilyNameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_FAMILY_NAME
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.1")
    String getFamilyName();
    void setFamilyName(String familyName);


    // -- givenName (property)

    class GivenNameDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = GivenNameDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_KNOWN_AS
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.2")
    String getGivenName();
    void setGivenName(String givenName);


    // -- knownAs (property)

    class KnownAsDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = KnownAsDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_KNOWN_AS
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="Name",
            sequence = "2.3")
    String getKnownAs();
    void setKnownAs(String knownAs);


    // -- emailAddress (property)

    class EmailAddressDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = EmailAddressDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_EMAIL_ADDRESS
    )
    @PropertyLayout(fieldSetId="Contact Details", sequence = "3.1")
    String getEmailAddress();
    void setEmailAddress(String emailAddress);


    // -- phoneNumber (property)

    class PhoneNumberDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = PhoneNumberDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_PHONE_NUMBER
    )
    @PropertyLayout(fieldSetId="Contact Details", sequence = "3.2")
    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);


    // -- faxNumber (property)

    class FaxNumberDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = FaxNumberDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_PHONE_NUMBER
    )
    @PropertyLayout(
            hidden=Where.PARENTED_TABLES,
            fieldSetId="Contact Details",
            sequence = "3.3")
    String getFaxNumber();
    void setFaxNumber(String faxNumber);


    // -- atPath (property)

    class AtPathDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = AtPathDomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = MAX_LENGTH_AT_PATH
    )
    @PropertyLayout(fieldSetId="atPath", sequence = "3.4")
    @Override
    String getAtPath();
    void setAtPath(String atPath);

    // -- accountType (property)

    class AccountTypeDomainEvent extends PropertyDomainEvent<AccountType> {}

    @Property(
            domainEvent = AccountTypeDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(fieldSetId="Status", sequence = "3")
    AccountType getAccountType();
    void setAccountType(AccountType accountType);


    // -- status (property)

    class StatusDomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}

    @Property(
            domainEvent = StatusDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(fieldSetId="Status", sequence = "4")
    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);


    // -- encryptedPassword (hidden property)

    @PropertyLayout(hidden=Where.EVERYWHERE)
    String getEncryptedPassword();
    void setEncryptedPassword(String encryptedPassword);



    // -- hasPassword (derived property)

    class HasPasswordDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = HasPasswordDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(fieldSetId="Status", sequence = "4")
    default boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }

    @Component
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    class HasPasswordAdvisor {

        final org.apache.isis.extensions.secman.api.user.ApplicationUserRepository<?> applicationUserRepository;

        @EventListener(org.apache.isis.extensions.secman.api.user.ApplicationUser.HasPasswordDomainEvent.class)
        public void advise(org.apache.isis.extensions.secman.api.user.ApplicationUser.HasPasswordDomainEvent ev) {
            switch(ev.getEventPhase()) {
                case HIDE:
                    if(! applicationUserRepository.isPasswordFeatureEnabled(ev.getSource())) {
                        ev.hide();
                    }
                    break;
            }
        }
    }





    // -- roles (collection)


    class RolesDomainEvent extends CollectionDomainEvent<ApplicationRole> {}

    @Collection(
            domainEvent = RolesDomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20")
    Set<? extends ApplicationRole> getRoles();




    /**
     * Short-term (request-scoped) caching.
     * @return
     */
    @Programmatic
    ApplicationPermissionValueSet getPermissionSet();

    @Programmatic
    boolean isForSelfOrRunAsAdministrator();

    @Programmatic
    default boolean isLocalAccount() {
        return getAccountType() == AccountType.LOCAL;
    }

}

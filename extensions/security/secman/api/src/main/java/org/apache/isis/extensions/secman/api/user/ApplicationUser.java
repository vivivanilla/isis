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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Parameter;
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
@DomainObject(
        objectType = "isis.ext.secman.IApplicationUser"
)
@DomainObjectLayout(
        titleUiEvent = ApplicationUser.TitleUiEvent.class,
        iconUiEvent = ApplicationUser.IconUiEvent.class,
        cssClassUiEvent = ApplicationUser.CssClassUiEvent.class,
        layoutUiEvent = ApplicationUser.LayoutUiEvent.class
)
public interface ApplicationUser
        extends HasUsername, HasAtPath, Comparable<ApplicationUser> {


    // -- DOMAIN EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationUser, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationUser, T> {}

    // -- UI EVENTS

    class TitleUiEvent extends IsisModuleExtSecmanApi.TitleUiEvent<ApplicationUser> {}
    class IconUiEvent extends IsisModuleExtSecmanApi.IconUiEvent<ApplicationUser> {}
    class CssClassUiEvent extends IsisModuleExtSecmanApi.CssClassUiEvent<ApplicationUser> {}
    class LayoutUiEvent extends IsisModuleExtSecmanApi.LayoutUiEvent<ApplicationUser> {}



    // -- username (property)

    @Property(
            domainEvent = Username.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Username.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="identity",
            sequence = "1"
    )
    @Parameter(
            maxLength = Username.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Username {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Username
    @Override
    String getUsername();
    void setUsername(String username);


    // -- accountType (property)

    @Property(
            domainEvent = AccountType.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AccountType {
        class DomainEvent extends PropertyDomainEvent<AccountType> {}
    }

    @AccountType
    org.apache.isis.extensions.secman.api.user.AccountType getAccountType();
    void setAccountType(org.apache.isis.extensions.secman.api.user.AccountType accountType);


    // -- status (property)

    @Property(
            domainEvent = Status.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Status {
        class DomainEvent extends PropertyDomainEvent<ApplicationUserStatus> {}
    }

    @Status
    ApplicationUserStatus getStatus();
    void setStatus(ApplicationUserStatus disabled);


    // -- atPath (property)

    @Property(
            domainEvent = AtPath.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = AtPath.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="status",
            sequence = "3"
    )
    @Parameter(
            maxLength = AtPath.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AtPath {
        int MAX_LENGTH = 254;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @AtPath
    @Override
    String getAtPath();
    void setAtPath(String atPath);


    // -- familyName (property)

    @Property(
            domainEvent = FamilyName.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = FamilyName.MAX_LENGTH
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "1"
    )
    @Parameter(
            maxLength = FamilyName.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface FamilyName {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FamilyName
    String getFamilyName();
    void setFamilyName(String familyName);


    // -- givenName (property)

    @Property(
            domainEvent = GivenName.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = GivenName.MAX_LENGTH
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "2"
    )
    @Parameter(
            maxLength = GivenName.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface GivenName {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @GivenName
    String getGivenName();
    void setGivenName(String givenName);


    // -- knownAs (property)

    @Property(
            domainEvent = KnownAs.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = KnownAs.MAX_LENGTH
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="name",
            sequence = "3"
    )
    @Parameter(
            maxLength = KnownAs.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface KnownAs {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @KnownAs
    String getKnownAs();
    void setKnownAs(String knownAs);


    // -- emailAddress (property)

    @Property(
            domainEvent = EmailAddress.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = EmailAddress.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId="contactDetails",
            sequence = "1"
    )
    @Parameter(
            maxLength = EmailAddress.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface EmailAddress {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @EmailAddress
    String getEmailAddress();
    void setEmailAddress(String emailAddress);


    // -- phoneNumber (property)

    @Property(
            domainEvent = PhoneNumber.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = PhoneNumber.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.PARENTED_TABLES,
            fieldSetId="contactDetails",
            sequence = "2"
    )
    @Parameter(
            maxLength = PhoneNumber.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface PhoneNumber {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @PhoneNumber
    String getPhoneNumber();
    void setPhoneNumber(String phoneNumber);


    // -- faxNumber (property)

    @Property(
            domainEvent = FaxNumber.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = FaxNumber.MAX_LENGTH
    )
    @PropertyLayout(
            hidden=Where.ALL_TABLES,
            fieldSetId="contactDetails",
            sequence = "3"
    )
    @Parameter(
            maxLength = FaxNumber.MAX_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface FaxNumber {
        int MAX_LENGTH = 120;
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @FaxNumber
    String getFaxNumber();
    void setFaxNumber(String faxNumber);


    // -- encryptedPassword (hidden property)

    @PropertyLayout(
            hidden=Where.EVERYWHERE,
            fieldSetId="password",
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface EncryptedPassword {
    }

    @EncryptedPassword
    String getEncryptedPassword();
    void setEncryptedPassword(String encryptedPassword);



    // -- hasPassword (derived property)

    @Property(
            domainEvent = HasPassword.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId="password",
            sequence = "1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface HasPassword {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }

    @HasPassword
    default boolean isHasPassword() {
        return _Strings.isNotEmpty(getEncryptedPassword());
    }


    @Component
    @Order(OrderPrecedence.LATE)
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    class HasPasswordAdvisor {

        final org.apache.isis.extensions.secman.api.user.ApplicationUserRepository applicationUserRepository;

        @EventListener(HasPassword.DomainEvent.class)
        public void advise(HasPassword.DomainEvent ev) {
            //noinspection SwitchStatementWithTooFewBranches
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

    @Collection(
            domainEvent = Roles.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Roles {
        class DomainEvent extends CollectionDomainEvent<ApplicationRole> {}
    }

    @Roles
    Set<ApplicationRole> getRoles();




    /**
     * Short-term (request-scoped) caching.
     */
    @Programmatic
    ApplicationPermissionValueSet getPermissionSet();

    @Programmatic
    boolean isForSelfOrRunAsAdministrator();

    @Programmatic
    default boolean isLocalAccount() {
        return getAccountType() == org.apache.isis.extensions.secman.api.user.AccountType.LOCAL;
    }

}

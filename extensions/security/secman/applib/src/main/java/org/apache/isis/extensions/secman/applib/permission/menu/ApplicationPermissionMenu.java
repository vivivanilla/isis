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
package org.apache.isis.extensions.secman.applib.permission.menu;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.permission.app.ApplicationOrphanedPermissionManager;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;

@Named(ApplicationPermissionMenu.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named="Security"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ApplicationPermissionMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationPermissionMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSecmanApplib.ActionDomainEvent<T> {}

    @Inject private ApplicationPermissionRepository applicationPermissionRepository;
    @Inject private FactoryService factoryService;


    @ObjectSupport public String iconName() {
        return "applicationPermission";
    }


    @Action(
            domainEvent= findOrphanedPermissions.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.50.1")
    public class findOrphanedPermissions{

        public class ActionDomainEvent extends ApplicationPermissionMenu.ActionDomainEvent<findOrphanedPermissions> {}

        @MemberSupport public ApplicationOrphanedPermissionManager act() {
            return factoryService.viewModel(new ApplicationOrphanedPermissionManager());
        }
    }



    @Action(
            domainEvent= allPermissions.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(sequence = "100.50.2")
    public class allPermissions {

        public class ActionDomainEvent extends ApplicationPermissionMenu.ActionDomainEvent<allPermissions> {}

        @MemberSupport public Collection<? extends ApplicationPermission> act() {
            return applicationPermissionRepository.allPermissions();
        }

    }


}

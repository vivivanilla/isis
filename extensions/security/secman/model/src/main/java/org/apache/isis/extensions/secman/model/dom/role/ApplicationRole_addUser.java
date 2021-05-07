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
package org.apache.isis.extensions.secman.model.dom.role;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.model.dom.role.ApplicationRole_addUser.ActionDomainEvent;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = ApplicationRole_addUser.ActionDomainEvent.class,
        associateWith = "users")
@ActionLayout(named="Add", sequence = "1")
@RequiredArgsConstructor
public class ApplicationRole_addUser {

    public static class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationRole_addUser> {}

    @Inject private ApplicationRoleRepository applicationRoleRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;

    private final ApplicationRole target;

    @MemberSupport
    public ApplicationRole act(final ApplicationUser applicationUser) {
        applicationRoleRepository.addRoleToUser(target, applicationUser);
        return target;
    }

    @MemberSupport
    public List<? extends ApplicationUser> autoComplete0Act(final String search) {
        val matchingSearch = applicationUserRepository.find(search);
        val list = _Lists.newArrayList(matchingSearch);
        list.removeAll(applicationUserRepository.findByRole(target));
        return list;
    }

}

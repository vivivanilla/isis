/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.commandlog.jpa.integtests.model;

import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        schema = "public",
        name = "Counter"
)
@Named("commandlog.test.Counter")
@DomainObject(nature = Nature.ENTITY)
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Counter extends org.apache.isis.extensions.commandlog.applib.integtest.model.Counter {

    @Id
    @GeneratedValue
    @Getter @Setter
    private Long id;

    @Column(nullable = false)
    @Getter @Setter
    private String name;

    @Column(nullable = true)
    @Getter @Setter
    private Long num;

    @Column(nullable = true)
    @Getter @Setter
    private Long num2;


}

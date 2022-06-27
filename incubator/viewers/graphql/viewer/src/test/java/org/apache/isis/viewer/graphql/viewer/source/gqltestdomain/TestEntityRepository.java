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
package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TestEntityRepository {

    @Inject
    private RepositoryService repositoryService;

    public E1 createE1(final Long id, final String name, @Nullable final E2 e2) {
        E1 e1 = new E1();
        e1.setId(id);
        e1.setName(name);
        e1.setE2(e2);
        repositoryService.persistAndFlush(e1);
        return e1;
    }

    public E2 createE2(final Long id, final String name, @Nullable final E1 e1) {
        E2 e2 = new E2();
        e2.setId(id);
        e2.setName(name);
        e2.setE1(e1);
        repositoryService.persistAndFlush(e2);
        return e2;
    }

    public List<E1> findAllE1() {
        return repositoryService.allInstances(E1.class);
    }

    public List<E2> findAllE2() {
        return repositoryService.allInstances(E2.class);
    }

    public List<TestEntity> findAllTestEntities() {
        final List<TestEntity> result = new ArrayList<>();
        result.addAll(findAllE1());
        result.addAll(findAllE2());
        return result;
    }

    public void removeAll(){
        findAllE1().forEach(e1->e1.setE2(null));
        findAllE2().forEach(e2->e2.setE1(null));
        repositoryService.removeAll(E1.class);
        repositoryService.removeAll(E2.class);
    }

    public E2 findE2ByName(final String name){
        return findAllE2().stream().filter(e2->e2.getName().equals(name)).findFirst().orElse(null);
    }

}

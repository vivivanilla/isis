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
package org.apache.isis.core.runtimeservices.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.unrecoverable.PersistFailedException;
import org.apache.isis.applib.exceptions.unrecoverable.RepositoryException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.query.ObjectBulkLoader;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ManagedObjects.UnwrapUtil;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".RepositoryServiceDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@RequiredArgsConstructor
//@Log4j2
public class RepositoryServiceDefault implements RepositoryService {

    final FactoryService factoryService;
    final WrapperFactory wrapperFactory;
    final TransactionService transactionService;
    final IsisConfiguration isisConfiguration;
    final ObjectManager objectManager;

    private boolean autoFlush;

    @PostConstruct
    public void init() {
        val disableAutoFlush = isisConfiguration.getCore().getRuntimeServices().getRepositoryService().isDisableAutoFlush();
        this.autoFlush = !disableAutoFlush;
    }

    @Override
    public EntityState getEntityState(final @Nullable Object object) {
        val adapter = objectManager.adapt(unwrapped(object));
        return EntityUtil.getEntityState(adapter);
    }

    @Override
    public <T> T detachedEntity(final @NonNull T entity) {
        return factoryService.detachedEntity(entity);
    }

    @Override
    public <T> T persist(final T domainObject) {

        val adapter = objectManager.adapt(unwrapped(domainObject));
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            throw new PersistFailedException("Object not known to framework (unable to create/obtain an adapter)");
        }
        // only persist detached entities, otherwise skip
        val entityState = EntityUtil.getEntityState(adapter);
        if(!entityState.isPersistable()
                || entityState.isAttached()) {
            return domainObject;
        }
        EntityUtil.persistInCurrentTransaction(adapter);
        return domainObject;
    }


    @Override
    public <T> T persistAndFlush(final T object) {
        persist(object);
        transactionService.flushTransaction();
        return object;
    }

    @Override
    public void remove(final Object domainObject) {
        if (domainObject == null) {
            return; // noop
        }
        val adapter = objectManager.adapt(unwrapped(domainObject));
        if(EntityUtil.isAttached(adapter)) {
            EntityUtil.destroyInCurrentTransaction(adapter);
        }
    }

    @Override
    public void removeAndFlush(final Object domainObject) {
        remove(domainObject);
        transactionService.flushTransaction();
    }


    // -- allInstances, allMatches, uniqueMatch, firstMatch

    @Override
    public <T> List<T> allInstances(final Class<T> type) {
        return allMatches(Query.<T>allInstances(type));
    }

    @Override
    public <T> List<T> allInstances(final Class<T> type, final long start, final long count) {
        return allMatches(Query.<T>allInstances(type)
                .withRange(QueryRange.of(start, count)));
    }

    @Override
    public <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate) {
        return allMatches(ofType, predicate, 0L, Long.MAX_VALUE);
    }


    @Override
    public <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, final long start, final long count) {
        return _NullSafe.stream(allInstances(ofType, start, count))
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        if(autoFlush) {
            transactionService.flushTransaction();
        }
        return submitQuery(query);
    }

    <T> List<T> submitQuery(final Query<T> query) {
        val resultTypeSpec = objectManager.getMetaModelContext()
                .getSpecificationLoader()
                .specForType(query.getResultType())
                .orElse(null);

        if(resultTypeSpec==null) {
            return Collections.emptyList();
        }

        val queryRequest = ObjectBulkLoader.Request.of(resultTypeSpec, query);
        val allMatching = objectManager.queryObjects(queryRequest);
        final List<T> resultList = _Casts.uncheckedCast(UnwrapUtil.multipleAsList(allMatching));
        return resultList;
    }

    @Override
    public <T> Optional<T> uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> Optional<T> firstMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate);
        return firstInstanceElseEmpty(instances);
    }


    @Override
    public <T> Optional<T> firstMatch(final Query<T> query) {
        final List<T> instances = allMatches(query);
        return firstInstanceElseEmpty(instances);
    }

    @Override
    public <T> T refresh(final T pojo) {
        val managedObject = objectManager.adapt(pojo);
        objectManager.getObjectRefresher().refreshObject(managedObject);
        return _Casts.uncheckedCast(managedObject.getPojo());
    }

    @Override
    public <T> T detach(final T entity) {
        val managedObject = objectManager.adapt(entity);
        val managedDetachedObject = objectManager.getObjectDetacher().detachObject(managedObject);
        return _Casts.uncheckedCast(managedDetachedObject.getPojo());
    }

    @Override
    public <T> void removeAll(final Class<T> cls) {
        allInstances(cls).forEach(this::remove);

    }

    // -- HELPER

    private static <T> Optional<T> firstInstanceElseEmpty(final List<T> instances) {
        return instances.size() == 0
                ? Optional.empty()
                : Optional.of(instances.get(0));
    }

    private Object unwrapped(final Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }


}

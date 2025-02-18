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
package org.apache.isis.core.metamodel.objectmanager.load;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
final class ObjectLoader_builtinHandlers {

    // -- NULL GUARD

    @Value
    public static class GuardAgainstNull implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {

            if(objectLoadRequest==null) {
                return true;
            }

            val spec = objectLoadRequest.getObjectSpecification();
            if(spec == null) {
                // eg "NONEXISTENT:123"
                return true;
            }

            // we don't guard against the identifier being null, because, this is ok
            // for services and values
            return false;
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {
            return null; // yes null
        }

    }

    // -- MANAGED BEANS

    @Value
    public static class LoadService implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isInjectable();
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            val logicalTypeName = spec.getLogicalTypeName();

            val servicePojo = metaModelContext.getServiceRegistry()
                .lookupRegisteredBeanById(logicalTypeName)
                .map(_ManagedBeanAdapter::getInstance)
                .flatMap(Can::getFirst)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "loader: %s loading beanName %s",
                        this.getClass().getName(), logicalTypeName));

            return ManagedObject.of(spec, servicePojo);
        }

    }

    // -- VALUES

    @Value
    public static class LoadValue implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isValue();
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {

            // cannot load a value

            val spec = objectLoadRequest.getObjectSpecification();
            throw _Exceptions.illegalArgument(
                    "cannot load a value, loader: %s loading ObjectSpecification %s",
                        this.getClass().getName(), spec);
        }

    }

    // -- VIEW MODELS

    @Value
    public static class LoadViewModel implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isViewModel();
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            val viewModelFacet = spec.getFacet(ViewModelFacet.class);
            if(viewModelFacet == null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing a ViewModelFacet: %s", spec);
            }

            val bookmark = objectLoadRequest.getBookmark();
            val viewModel = viewModelFacet.instantiate(spec, Optional.of(bookmark));
            return ManagedObject.bookmarked(spec, viewModel.getPojo(), bookmark);
        }

    }

    // -- ENTITIES

    @Value
    public static class LoadEntity implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            return spec.isEntity();
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {

            val spec = objectLoadRequest.getObjectSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                throw _Exceptions.illegalArgument(
                        "ObjectSpecification is missing an EntityFacet: %s", spec);
            }

            val bookmark = objectLoadRequest.getBookmark();
            val entity = entityFacet.fetchByIdentifier(bookmark);
            return entity;
        }

    }

    // -- UNKNOWN LOAD REQUEST

    @Value
    public static class LoadOther implements ObjectLoader.Handler {

        private final @NonNull MetaModelContext metaModelContext;

        @Override
        public boolean isHandling(final ObjectLoader.Request objectLoadRequest) {
            return true; // the last handler in the chain
        }

        @Override
        public ManagedObject handle(final ObjectLoader.Request objectLoadRequest) {

            // unknown object load request

             throw _Exceptions.illegalArgument(
                    "None of the registered ObjectLoaders knows how to load this object. (loader: %s loading %s)",
                        this.getClass().getName(), objectLoadRequest);

        }

    }

}

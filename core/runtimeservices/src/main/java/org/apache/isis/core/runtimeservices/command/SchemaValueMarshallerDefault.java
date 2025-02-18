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
package org.apache.isis.core.runtimeservices.command;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshallerAbstract;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".SchemaValueMarshallerDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class SchemaValueMarshallerDefault
extends SchemaValueMarshallerAbstract {

    @Inject private ValueSemanticsResolver valueSemanticsResolver;
    @Inject private SpecificationLoader specLoader;

    // -- RECORD VALUES INTO DTO

    @Override
    protected <T> ValueWithTypeDto recordValue(
            final Context<T> context,
            final ValueWithTypeDto valueDto,
            final ManagedObject value) {

        valueDto.setType(context.getSchemaValueType());

        switch (context.getSchemaValueType()) {
        case COMPOSITE:
            valueDto.setComposite(toTypedTuple(context, _Casts.<T>uncheckedCast(value.getPojo())));
            return valueDto;
        case COLLECTION:
            recordValues(context, valueDto, ((PackedManagedObject)value).unpack());
            return valueDto;
        case REFERENCE:
            value.getBookmark()
                .map(Bookmark::toOidDto)
                .ifPresent(valueDto::setReference); // otherwise: null reference
            return valueDto;

        default:
            break;
        }

        val decomposedValueDto = context.getSemantics()
                .decompose(_Casts.uncheckedCast(value.getPojo()))
                .leftIfAny();

        // copy the decomposedValueDto into valueDto
        CommonDtoUtils.copy(decomposedValueDto, valueDto);

        return valueDto;
    }

    @Override
    protected <T> ValueWithTypeDto recordValues(
            final Context<T> context,
            final ValueWithTypeDto valueWithTypeDto,
            final Can<ManagedObject> values) {

        valueWithTypeDto.setType(ValueType.COLLECTION);
        valueWithTypeDto.setCollection(asCollectionDto(context, values));
        valueWithTypeDto.setNull(false);
        return valueWithTypeDto;
    }

    private <T> CollectionDto asCollectionDto(
            final Context<T> context,
            final Can<ManagedObject> values) {

        val elementValueType = context.getSchemaValueType();
        val collectionDto = new CollectionDto();
        collectionDto.setType(elementValueType);

        values.stream()
        .forEach(element->{
            val valueDto = new ValueWithTypeDto();
            valueDto.setType(elementValueType);
            collectionDto.getValue().add(valueDto);
            recordValue(context, valueDto, element);
        });

        return collectionDto;
    }

    private <T> TypedTupleDto toTypedTuple(final Context<T> context, final T valuePojo) {
        return valuePojo!=null
                ? context.getSemantics()
                        .decompose(valuePojo)
                        .rightIfAny()
                : null;
    }

    // -- RECOVER VALUES FROM DTO

    @Override
    protected ManagedObject recoverScalarValue(
            @NonNull final Context<?> context,
            @NonNull final ValueWithTypeDto valueDto) {

        val elementSpec = context.getElementType();

        val recoveredValueAsPojo = valueDto.getType()==ValueType.COMPOSITE
                ? fromTypedTuple(context, valueDto.getComposite())
                : context.getSemantics().compose(ValueDecomposition.ofFundamental(valueDto));

        if(recoveredValueAsPojo==null) {
            return ManagedObject.empty(context.getElementType());
        }

        val recoveredValue = recoveredValueAsPojo!=null
                ? ManagedObject.of(elementSpec, recoveredValueAsPojo)
                : ManagedObject.empty(context.getElementType());
        return recoveredValue;
    }

    private <T> T fromTypedTuple(final Context<T> context, final TypedTupleDto typedTupleDto) {
        if(typedTupleDto==null) {
            return null;
        }
        return context.getSemantics()
                .compose(ValueDecomposition.ofComposite(typedTupleDto));
    }

    // -- DEPENDENCIES

    @Override
    protected final SpecificationLoader getSpecificationLoader() {
        return specLoader;
    }

    @Override
    protected ValueSemanticsResolver getValueSemanticsResolver() {
        return valueSemanticsResolver;
    }

}

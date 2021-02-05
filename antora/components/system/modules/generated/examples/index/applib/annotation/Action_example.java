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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.inject.Inject;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.iactn.Interaction.Execution;
import org.apache.isis.applib.services.publishing.spi.CommandSubscriber;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

import lombok.Builder;
import lombok.val;

public class Action_example {

    public static class Product { }

    @Builder
    public static class Order {
        private Product product;
        private int quantity;
    }

    public static class Customer {

        public static class OrderPlaced extends ActionDomainEvent<Customer> {}

        @Action(
                semantics = SemanticsOf.NON_IDEMPOTENT // <.>
                , domainEvent = OrderPlaced.class // <.>
                , executionPublishing = Publishing.ENABLED // <.>
                , associateWith = "orders" // <.>
        )
        public Order placeOrder(Product product, int quantity) {
            return Order.builder()
                    .product(product)
                    .quantity(quantity)
                    .build();
        }
    }
}

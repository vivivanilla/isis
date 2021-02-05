package org.apache.isis.applib.services.clock;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;

import lombok.Builder;
import lombok.val;

public class ClockService_example {

    public static class Product { }

    @Builder
    public static class Order {
        private Product product;
        private int quantity;
        private LocalDateTime placedAt;
    }

    public static class Customer {

        @Action
        public Order placeOrder(Product product, int quantity) {
            val now = clockService.getClock()
                                  .localDateTime(ZoneId.systemDefault()); // <.>
            return Order.builder()
                    .product(product)
                    .quantity(quantity)
                    .placedAt(now)
                    .build();
        }
        @Inject ClockService clockService;
    }
}


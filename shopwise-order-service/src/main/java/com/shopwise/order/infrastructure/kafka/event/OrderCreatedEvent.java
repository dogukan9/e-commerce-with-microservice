package com.shopwise.order.infrastructure.kafka.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        String shippingAddress
) {}

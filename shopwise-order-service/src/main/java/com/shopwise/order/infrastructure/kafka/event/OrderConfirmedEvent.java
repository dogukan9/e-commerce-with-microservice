package com.shopwise.order.infrastructure.kafka.event;

import java.util.List;

public record OrderConfirmedEvent(
        Long orderId,
        List<OrderItemEvent> items
) {}

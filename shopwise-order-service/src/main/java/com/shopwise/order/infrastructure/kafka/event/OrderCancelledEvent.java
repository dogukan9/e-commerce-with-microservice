package com.shopwise.order.infrastructure.kafka.event;

import java.util.List;

public record OrderCancelledEvent(
        Long orderId,
        List<OrderItemEvent> items
) {}

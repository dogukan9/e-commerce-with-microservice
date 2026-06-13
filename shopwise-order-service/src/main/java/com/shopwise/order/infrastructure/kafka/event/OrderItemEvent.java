package com.shopwise.order.infrastructure.kafka.event;

import java.math.BigDecimal;

public record OrderItemEvent(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice
) {}

package com.shopwise.order.infrastructure.kafka.event;

import java.math.BigDecimal;

public record StockReservedEvent(
        Long orderId,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
) {}

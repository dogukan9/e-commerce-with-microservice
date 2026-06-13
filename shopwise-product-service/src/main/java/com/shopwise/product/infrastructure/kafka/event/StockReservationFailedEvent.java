package com.shopwise.product.infrastructure.kafka.event;

public record StockReservationFailedEvent(
        Long orderId,
        Long productId,
        String reason
) {}

package com.shopwise.order.application.dto;

import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        List<OrderItemResponse> items,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        AuditUserResponse createdBy,
        AuditUserResponse updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order,
                                     AuditUserResponse createdBy,
                                     AuditUserResponse updatedBy) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                createdBy,
                updatedBy,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
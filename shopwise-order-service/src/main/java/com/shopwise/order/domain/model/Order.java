package com.shopwise.order.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class Order {

    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Order create(Long userId, List<OrderItem> items, String shippingAddress) {
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Order.builder()
                .userId(userId)
                .items(items)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(shippingAddress)
                .build();
     }


    public Order withUpdatedBy(Long userId) {
        return Order.builder()
                .id(this.id)
                .userId(this.userId)
                .items(this.items)
                .status(this.status)
                .totalAmount(this.totalAmount)
                .shippingAddress(this.shippingAddress)
                .createdBy(this.createdBy)
                .updatedBy(userId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    public void markStockReserved() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Sadece PENDING siparişler STOCK_RESERVED yapılabilir");
        }
        this.status = OrderStatus.STOCK_RESERVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == OrderStatus.CONFIRMED ||
            this.status == OrderStatus.SHIPPED ||
            this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Bu durumdaki sipariş iptal edilemez: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != OrderStatus.STOCK_RESERVED) {
            throw new IllegalStateException("Sadece STOCK_RESERVED siparişler onaylanabilir");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }
}

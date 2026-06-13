package com.shopwise.order.infrastructure.persistence;

import com.shopwise.order.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    public static OrderItemEntity fromDomain(OrderItem item, OrderEntity orderEntity) {
        return OrderItemEntity.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .order(orderEntity)
                .build();
    }

    public OrderItem toDomain() {
        return OrderItem.builder()
                .id(id)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}

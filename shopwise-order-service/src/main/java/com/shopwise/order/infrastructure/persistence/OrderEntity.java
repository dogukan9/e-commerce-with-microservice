package com.shopwise.order.infrastructure.persistence;

import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String shippingAddress;

    public static OrderEntity fromDomain(Order order) {
        OrderEntity entity = OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .build();

        entity.setCreatedBy(order.getCreatedBy());
        entity.setUpdatedBy(order.getUpdatedBy());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> OrderItemEntity.fromDomain(item, entity))
                .toList();
        entity.setItems(new ArrayList<>(itemEntities));

        return entity;
    }

    public Order toDomain() {
        return Order.builder()
                .id(id)
                .userId(userId)
                .items(items.stream().map(OrderItemEntity::toDomain).toList())
                .status(status)
                .totalAmount(totalAmount)
                .shippingAddress(shippingAddress)
                .createdBy(getCreatedBy())
                .updatedBy(getUpdatedBy())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}

package com.shopwise.order.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItem {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public static OrderItem create(Long productId, String productName,
                                    Integer quantity, BigDecimal unitPrice) {
        return OrderItem.builder()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}

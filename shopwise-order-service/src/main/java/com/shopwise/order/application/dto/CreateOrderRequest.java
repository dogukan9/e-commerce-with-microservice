package com.shopwise.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Sipariş kalemi boş olamaz")
        List<OrderItemRequest> items,

        @NotBlank(message = "Teslimat adresi boş olamaz")
        String shippingAddress
) {}

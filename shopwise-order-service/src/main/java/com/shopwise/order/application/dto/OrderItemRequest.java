package com.shopwise.order.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "Ürün id boş olamaz")
        Long productId,

        @NotNull(message = "Miktar boş olamaz")
        @Min(value = 1, message = "Miktar en az 1 olmalıdır")
        Integer quantity
) {}

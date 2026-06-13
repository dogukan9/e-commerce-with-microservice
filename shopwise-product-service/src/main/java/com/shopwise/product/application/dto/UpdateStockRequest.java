package com.shopwise.product.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @NotNull(message = "Miktar boş olamaz")
        @Min(value = 1, message = "Miktar en az 1 olmalıdır")
        Integer quantity
) {}

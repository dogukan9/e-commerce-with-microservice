package com.shopwise.product.application.dto;

import com.shopwise.product.domain.model.ProductCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Ürün adı boş olamaz")
        String name,

        @NotBlank(message = "Açıklama boş olamaz")
        String description,

        @NotNull(message = "Fiyat boş olamaz")
        @DecimalMin(value = "0.0", inclusive = false, message = "Fiyat 0'dan büyük olmalıdır")
        BigDecimal price,

        @NotNull(message = "Stok boş olamaz")
        @Min(value = 0, message = "Stok 0'dan küçük olamaz")
        Integer stock,

        @NotNull(message = "Kategori boş olamaz")
        ProductCategory category
) {}

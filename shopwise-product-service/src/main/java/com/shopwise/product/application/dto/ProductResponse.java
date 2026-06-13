package com.shopwise.product.application.dto;

import com.shopwise.product.domain.model.Product;
import com.shopwise.product.domain.model.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Integer reservedStock,
        Integer availableStock,
        ProductCategory category,
        boolean active,
        AuditUserResponse createdBy,
        AuditUserResponse updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product,
                                       AuditUserResponse createdBy,
                                       AuditUserResponse updatedBy) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getReservedStock(),
                product.getAvailableStock(),
                product.getCategory(),
                product.isActive(),
                createdBy,
                updatedBy,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
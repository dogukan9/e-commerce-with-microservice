package com.shopwise.product.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer reservedStock;
    private ProductCategory category;
    private boolean active;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getAvailableStock() {
        return this.stock - this.reservedStock;
    }

    public static Product create(String name, String description,
                                 BigDecimal price, Integer stock,
                                 ProductCategory category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .reservedStock(0)
                .category(category)
                .active(true)
                .build();
    }

    public Product withUpdatedBy(Long userId) {
        return Product.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .stock(this.stock)
                .reservedStock(this.reservedStock)
                .category(this.category)
                .active(this.active)
                .createdBy(this.createdBy)
                .updatedBy(userId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
    public void reserve(int quantity) {
        if (getAvailableStock() < quantity) {
            throw new IllegalArgumentException(
                    "Yetersiz stok. Mevcut: " + getAvailableStock() + ", İstenen: " + quantity);
        }
        this.reservedStock += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void releaseReservation(int quantity) {
        this.reservedStock = Math.max(0, this.reservedStock - quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmReservation(int quantity) {
        this.stock -= quantity;
        this.reservedStock = Math.max(0, this.reservedStock - quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
        this.updatedAt = LocalDateTime.now();
    }
}

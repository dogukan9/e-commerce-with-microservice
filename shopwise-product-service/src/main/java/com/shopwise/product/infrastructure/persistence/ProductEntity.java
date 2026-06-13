package com.shopwise.product.infrastructure.persistence;

import com.shopwise.product.domain.model.Product;
import com.shopwise.product.domain.model.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer reservedStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(nullable = false)
    private boolean active;

    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .reservedStock(product.getReservedStock())
                .category(product.getCategory())
                .active(product.isActive())
                .build();

        entity.setCreatedBy(product.getCreatedBy());
        entity.setUpdatedBy(product.getUpdatedBy());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());

        return entity;
    }

    public Product toDomain() {
        return Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .reservedStock(reservedStock)
                .category(category)
                .active(active)
                .createdBy(getCreatedBy())
                .updatedBy(getUpdatedBy())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}

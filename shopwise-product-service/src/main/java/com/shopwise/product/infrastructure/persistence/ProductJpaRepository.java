package com.shopwise.product.infrastructure.persistence;

import com.shopwise.product.domain.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    List<ProductEntity> findByCategory(ProductCategory category);
}

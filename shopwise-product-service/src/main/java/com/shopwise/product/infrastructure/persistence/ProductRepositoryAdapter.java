package com.shopwise.product.infrastructure.persistence;

import com.shopwise.product.domain.model.Product;
import com.shopwise.product.domain.model.ProductCategory;
import com.shopwise.product.domain.port.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(ProductEntity.fromDomain(product)).toDomain();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id).map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream().map(ProductEntity::toDomain).toList();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return productJpaRepository.findByCategory(ProductCategory.valueOf(category))
                .stream().map(ProductEntity::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        productJpaRepository.deleteById(id);
    }
}

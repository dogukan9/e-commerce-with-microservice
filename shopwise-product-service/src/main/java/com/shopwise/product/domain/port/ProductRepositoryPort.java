package com.shopwise.product.domain.port;

import com.shopwise.product.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
    List<Product> findByCategory(String category);
    void deleteById(Long id);
}

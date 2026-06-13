package com.shopwise.product.application.port;

import com.shopwise.product.application.dto.*;

import java.util.List;

public interface ProductUseCase {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getProductsByCategory(String category);
    ProductResponse increaseStock(Long id, UpdateStockRequest request);
    void deleteProduct(Long id);
    void reserveStock(Long productId, Integer quantity);
    void releaseReservation(Long productId, Integer quantity);
    void confirmReservation(Long productId, Integer quantity);
}

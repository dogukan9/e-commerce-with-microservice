package com.shopwise.product.api;

import com.shopwise.product.application.dto.*;
import com.shopwise.product.application.port.ProductUseCase;
import com.shopwise.product.infrastructure.kafka.DeadLetterRetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;
    private final DeadLetterRetryService deadLetterRetryService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productUseCase.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productUseCase.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productUseCase.getAllProducts());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productUseCase.getProductsByCategory(category));
    }

    @PatchMapping("/{id}/increase-stock")
    public ResponseEntity<ProductResponse> increaseStock(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(productUseCase.increaseStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productUseCase.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Servisler arası internal endpoint
    @GetMapping("/internal/{id}")
    public ResponseEntity<ProductResponse> getProductForInternal(@PathVariable Long id) {
        return ResponseEntity.ok(productUseCase.getProductById(id));
    }

    // Manuel dead letter retry
    @PostMapping("/dead-letters/{id}/retry")
    public ResponseEntity<Void> retryDeadLetter(@PathVariable Long id) {
        deadLetterRetryService.retryEvent(id);
        return ResponseEntity.ok().build();
    }
}

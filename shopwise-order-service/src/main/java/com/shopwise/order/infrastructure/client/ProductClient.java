package com.shopwise.order.infrastructure.client;

import com.shopwise.order.infrastructure.client.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "shopwise-product-service", url = "${services.gateway.url}")
public interface ProductClient {

    @GetMapping("/api/v1/products/internal/{id}")
    ProductClientResponse getProductById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization
    );
}
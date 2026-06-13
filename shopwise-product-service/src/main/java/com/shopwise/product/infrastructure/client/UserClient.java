package com.shopwise.product.infrastructure.client;

import com.shopwise.product.infrastructure.client.dto.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "shopwise-user-service", url = "${services.gateway.url}")
public interface UserClient {

    @GetMapping("/api/v1/users/internal/{id}")
    UserClientResponse getUserById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization
    );
}
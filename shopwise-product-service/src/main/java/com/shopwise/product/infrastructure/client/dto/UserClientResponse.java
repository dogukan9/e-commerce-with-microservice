package com.shopwise.product.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserClientResponse(
        Long id,
        String fullName
) {}
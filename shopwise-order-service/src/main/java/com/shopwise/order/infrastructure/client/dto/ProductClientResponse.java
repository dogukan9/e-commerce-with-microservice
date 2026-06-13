package com.shopwise.order.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductClientResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer availableStock,
        boolean active
) {}

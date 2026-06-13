package com.shopwise.order.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserClientResponse(
        Long id,
        String email,
        String fullName,
        boolean active
) {}

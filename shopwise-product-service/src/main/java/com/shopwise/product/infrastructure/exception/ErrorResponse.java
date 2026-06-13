package com.shopwise.product.infrastructure.exception;

public record ErrorResponse(
        String errorCode,
        String message
) {}

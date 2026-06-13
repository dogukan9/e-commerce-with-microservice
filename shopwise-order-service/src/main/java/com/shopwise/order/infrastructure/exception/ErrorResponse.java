package com.shopwise.order.infrastructure.exception;

public record ErrorResponse(
        String errorCode,
        String message
) {}

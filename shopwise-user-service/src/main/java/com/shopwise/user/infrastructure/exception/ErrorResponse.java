package com.shopwise.user.infrastructure.exception;

public record ErrorResponse(
        String errorCode,
        String message
) {}

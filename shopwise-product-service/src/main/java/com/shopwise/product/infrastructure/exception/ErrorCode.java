package com.shopwise.product.infrastructure.exception;

public final class ErrorCode {

    private ErrorCode() {}

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
}

package com.shopwise.order.infrastructure.exception;

public final class ErrorCode {

    private ErrorCode() {}

    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND";
    public static final String USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public static final String PRODUCT_NOT_ACTIVE = "PRODUCT_NOT_ACTIVE";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
}

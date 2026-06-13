package com.shopwise.user.infrastructure.exception;

public final class ErrorCode {

    private ErrorCode() {}

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String FORBIDDEN = "FORBIDDEN";
}

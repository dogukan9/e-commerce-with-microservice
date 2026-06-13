package com.shopwise.user.application.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {}

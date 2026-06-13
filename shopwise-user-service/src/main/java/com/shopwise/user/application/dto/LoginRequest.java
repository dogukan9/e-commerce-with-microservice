package com.shopwise.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email giriniz")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        String password
) {}

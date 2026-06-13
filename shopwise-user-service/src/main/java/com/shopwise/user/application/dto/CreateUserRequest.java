package com.shopwise.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçerli bir email giriniz")
        String email,

        @NotBlank(message = "Şifre boş olamaz")
        @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
        String password,

        @NotBlank(message = "Ad soyad boş olamaz")
        String fullName
) {}

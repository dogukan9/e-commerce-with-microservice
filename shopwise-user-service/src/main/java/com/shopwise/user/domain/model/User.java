package com.shopwise.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class User {

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private UserRole role;
    private boolean active;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static User create(String email, String password, String fullName) {
        return User.builder()
                .email(email)
                .password(password)
                .fullName(fullName)
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();
    }

    public User withUpdatedBy(Long userId) {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .fullName(this.fullName)
                .role(this.role)
                .active(this.active)
                .createdBy(this.createdBy)
                .updatedBy(userId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

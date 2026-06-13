package com.shopwise.user.application.dto;

import com.shopwise.user.domain.model.User;
import com.shopwise.user.domain.model.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        UserRole role,
        boolean active,
        AuditUserResponse createdBy,
        AuditUserResponse updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user, User createdByUser, User updatedByUser) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                createdByUser != null ? new AuditUserResponse(createdByUser.getId(), createdByUser.getFullName()) : null,
                updatedByUser != null ? new AuditUserResponse(updatedByUser.getId(), updatedByUser.getFullName()) : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
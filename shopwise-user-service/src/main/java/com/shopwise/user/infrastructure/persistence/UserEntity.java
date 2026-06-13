package com.shopwise.user.infrastructure.persistence;

import com.shopwise.user.domain.model.User;
import com.shopwise.user.domain.model.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean active;


    public static UserEntity fromDomain(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.isActive())
                .build();

        entity.setCreatedBy(user.getCreatedBy());
        entity.setUpdatedBy(user.getUpdatedBy());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());

        return entity;
    }
    public User toDomain() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .fullName(fullName)
                .role(role)
                .active(active)
                .createdBy(getCreatedBy())
                .updatedBy(getUpdatedBy())
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}

package com.shopwise.user.application.service;

import com.shopwise.user.application.dto.*;
import com.shopwise.user.application.port.UserUseCase;
import com.shopwise.user.domain.model.User;
import com.shopwise.user.domain.port.UserRepositoryPort;
import com.shopwise.user.infrastructure.config.JwtService;
import com.shopwise.user.infrastructure.exception.BusinessException;
import com.shopwise.user.infrastructure.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepositoryPort.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Bu email zaten kullanımda: " + request.email());
        }

        User user = User.create(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName()
        );
        user = setUpdatedBy(user);
        return buildUserResponse(userRepositoryPort.save(user));
    }

    private UserResponse buildUserResponse(User user) {
        User createdByUser = user.getCreatedBy() != null ?
                userRepositoryPort.findById(user.getCreatedBy()).orElse(null) : null;

        User updatedByUser = user.getUpdatedBy() != null ?
                userRepositoryPort.findById(user.getUpdatedBy()).orElse(null) : null;

        return UserResponse.from(user, createdByUser, updatedByUser);
    }
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "Kullanıcı bulunamadı: " + id));
        return buildUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "Kullanıcı bulunamadı: " + email));
        return buildUserResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepositoryPort.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "Kullanıcı bulunamadı: " + request.email()));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Email veya şifre hatalı");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        String redisKey = "whitelist:" + user.getId();
        redisTemplate.opsForValue().set(redisKey, token,
                java.time.Duration.ofMillis(jwtExpiration));

        return new AuthResponse(token, buildUserResponse(user));
    }

    private User setUpdatedBy(User user) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getName())) {
                return user.withUpdatedBy(Long.valueOf(auth.getName()));
            }
        } catch (Exception e) {
            log.warn("updatedBy set edilemedi: {}", e.getMessage());
        }
        return user;
    }
}

package com.shopwise.user.application.port;

import com.shopwise.user.application.dto.*;

public interface UserUseCase {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    AuthResponse login(LoginRequest request);
}

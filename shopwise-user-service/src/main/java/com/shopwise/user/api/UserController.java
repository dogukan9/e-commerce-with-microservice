package com.shopwise.user.api;

import com.shopwise.user.application.dto.*;
import com.shopwise.user.application.port.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userUseCase.createUser(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userUseCase.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userUseCase.getUserByEmail(email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userUseCase.login(request));
    }

    // Servisler arası internal endpoint — gateway tarafından dışarıya kapatılacak
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserResponse> getUserForInternal(@PathVariable Long id) {
        return ResponseEntity.ok(userUseCase.getUserById(id));
    }
}

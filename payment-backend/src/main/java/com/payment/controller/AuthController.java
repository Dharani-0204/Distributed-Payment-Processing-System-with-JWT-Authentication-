package com.payment.controller;

import com.payment.dto.AuthResponse;
import com.payment.dto.LoginRequest;
import com.payment.dto.UserRequest;
import com.payment.dto.UserResponse;
import com.payment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest signUpRequest) {
        return ResponseEntity.ok(authService.register(signUpRequest));
    }
}

package com.payment.service;

import com.payment.dto.AuthResponse;
import com.payment.dto.LoginRequest;
import com.payment.dto.UserRequest;
import com.payment.dto.UserResponse;
import com.payment.entity.User;
import com.payment.repository.UserRepository;
import com.payment.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt().toString())
                .build();

        return AuthResponse.builder()
                .token(jwt)
                .user(userResponse)
                .build();
    }

    public UserResponse register(UserRequest userRequest) {
        return userService.createUser(userRequest);
    }
}

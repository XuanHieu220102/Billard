package com.billiard.app.auth.controller;

import com.billiard.app.auth.dto.AuthResponse;
import com.billiard.app.auth.dto.LoginRequest;
import com.billiard.app.auth.dto.RegisterRequest;
import com.billiard.app.auth.service.AuthService;
import com.billiard.app.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        // Stateless JWT: nothing to invalidate server-side in MVP scope.
        // The client is responsible for discarding the stored token.
        return ApiResponse.success(null, "Logged out successfully");
    }
}

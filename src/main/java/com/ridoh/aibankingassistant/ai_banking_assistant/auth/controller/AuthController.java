package com.ridoh.aibankingassistant.ai_banking_assistant.auth.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.*;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.service.AuthService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Authentication and authorization APIs"
)
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered successfully",
                        response
                ));
    }

    @Operation(summary = "Register a new administrator")
    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(
            @Valid @RequestBody AdminRegisterRequest request) {

        AuthResponse response = authService.registerAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Admin registered successfully",
                        response
                ));
    }

    @Operation(summary = "Authenticate user")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Access token refreshed successfully",
                        response
                )
        );
    }
}
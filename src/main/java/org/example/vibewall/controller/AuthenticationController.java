package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.TokenRequested;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, and logout")
public class AuthenticationController {

    private final RegistrationService service;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or user already exists")
    public ResponseEntity<UsersResponse> register(
            @Valid @RequestBody UsersRequested user) {
        return new ResponseEntity<>(service.adduser(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT tokens")
    @ApiResponse(responseCode = "200", description = "Login successful — returns access and refresh tokens")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody UsersRequested user) {
        return ResponseEntity.ok(service.login(user));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using a refresh token")
    @ApiResponse(responseCode = "200", description = "New access token issued")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody TokenRequested requested) {
        return ResponseEntity.ok(service.refresh(requested.getToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate refresh token")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public ResponseEntity<String> logout() {
        service.logout();
        return ResponseEntity.ok("Logged out successfully");
    }
}

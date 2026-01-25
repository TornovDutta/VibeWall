package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.TokenRequested;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegistrationService service;

    @PostMapping("/register")
    public ResponseEntity<UsersResponse> register(
            @Valid @RequestBody UsersRequested user) {

        return new ResponseEntity<>(service.adduser(user), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody UsersRequested user) {

        return ResponseEntity.ok(service.login(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody TokenRequested requested) {
        String token=requested.getToken();
        return ResponseEntity.ok(service.refresh(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {


        service.logout();

        return ResponseEntity.ok("Logged out successfully");
    }
}

package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Manage your own user profile")
public class UserController {
    private final UsersService service;

    @PutMapping("/me")
    @Operation(summary = "Update your profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserResponse> updateUser(
            @Valid @RequestBody UserRequest user,
            @AuthenticationPrincipal CustomUserDetails details) throws UserNotFoundException {
        return new ResponseEntity<>(service.update(details.getId(), user), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete your account")
    @ApiResponse(responseCode = "204", description = "Account deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails details) throws UserNotFoundException {
        service.delete(details.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

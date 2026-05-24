package org.example.vibewall.service;

import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;

public interface RegistrationService {
    UserResponse adduser(UserRequest user);

    TokenResponse login(UserRequest user);

    void logout();

    TokenResponse refresh(String token);
}

package org.example.vibewall.service;

import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.model.Users;

public interface RegistrationService {
    UsersResponse adduser(UsersRequested user);

    TokenResponse login(UsersRequested user);

    void logout();

    TokenResponse refresh(String token);
}
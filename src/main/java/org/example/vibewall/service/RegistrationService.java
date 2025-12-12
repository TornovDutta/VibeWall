package org.example.vibewall.service;

import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.model.Users;

public interface RegistrationService {
    UsersResponse adduser(UsersRequested user);
}

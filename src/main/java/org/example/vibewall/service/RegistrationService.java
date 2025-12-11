package org.example.vibewall.service;

import org.example.vibewall.DTO.UsersRequestedDTO;
import org.example.vibewall.DTO.UsersResponseDTO;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;

public interface RegistrationService {
    UsersResponseDTO adduser(UsersRequestedDTO user) throws UserNotFoundException;
}

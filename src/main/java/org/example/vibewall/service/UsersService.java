package org.example.vibewall.service;

import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.exception.UserNotFoundException;

public interface UsersService {
    UsersResponse update(String id, UsersRequested user);

    void delete(String id) throws UserNotFoundException;
}

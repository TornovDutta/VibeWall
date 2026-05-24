package org.example.vibewall.service;

import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.exception.UserNotFoundException;

public interface UsersService {
    UserResponse update(String id, UserRequest user);

    void delete(String id) throws UserNotFoundException;
}

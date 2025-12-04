package org.example.vibewall.service;

import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;

public interface UsersService {
    Users update(String id, Users user);
    void delete(String id) throws UserNotFoundException;
}

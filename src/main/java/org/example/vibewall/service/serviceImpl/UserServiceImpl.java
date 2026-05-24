package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UsersService {

    private final UserRepository repo;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final Encryption encryption;

    public UserResponse update(String id, UserRequest user) {

        Users existingUser = repo.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getName()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(existingUser);
        return mapper.toDTO(existingUser);
    }


    public void delete(String id) throws UserNotFoundException {
        repo.findById(id).orElseThrow(()->
                new UserNotFoundException("user of id: " + id + "not found"));

        repo.removeById(id);
    }

}

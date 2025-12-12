package org.example.vibewall.service;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepo repo;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final static Logger logger= LoggerFactory.getLogger(UsersService.class);
    private final Encryption encryption;

    public UsersResponse update(String id, UsersRequested user) {

        Users existingUser = repo.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.name()));
        existingUser.setPassword(passwordEncoder.encode(user.password()));
        return mapper.toDTO(existingUser);
    }


    public void delete(String id) throws UserNotFoundException {
       repo.findById(id).orElseThrow(()->
               new UserNotFoundException("user of id: "+id+"not found"));
       logger.info("user remove");
       repo.removeById(id);
    }



}

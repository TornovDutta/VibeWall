package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UsersServiceImple implements UsersService {

    private final UsersRepo repo;

    private final PasswordEncoder passwordEncoder;
    private final static Logger logger= LoggerFactory.getLogger(UsersServiceImple.class);
    private final Encryption encryption;
    @Override
    public Users update(String id, Users user) {

        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        existingUser.setUsername(encryption.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(existingUser);

    }

    @Override
    public void delete(String id) throws UserNotFoundException {
       repo.findById(id).orElseThrow(()->
               new UserNotFoundException("user of id: "+id+"not found"));
       logger.info("id: "+id+" ,user remove");
       repo.removeById(id);
    }



}

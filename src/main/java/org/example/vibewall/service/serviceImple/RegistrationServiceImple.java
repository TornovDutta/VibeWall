package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImple implements RegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final UsersRepo repo;
    private final static Logger logger= LoggerFactory.getLogger(RegistrationServiceImple.class);
    private final Encryption encryption;

    @Override
    public Users adduser(Users user){
        user.setUsername(encryption.encode(user.getUsername()));
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        logger.info("new User add");
        return repo.save(user);
    }


}

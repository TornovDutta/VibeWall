package org.example.vibewall.service.serviceImplement;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.RegistrationService;
import org.example.vibewall.utility.UserMapper;
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
    private final UserMapper mapper;

    @Override
    public UsersResponse adduser(UsersRequested user){

        String name= encryption.encode(user.name());
        String password=passwordEncoder.encode(user.password());

        Users newUser=new Users(name,password,"USER");
        logger.info("new user add");
        repo.save(newUser);

        return mapper.toDTO(newUser);
    }


}

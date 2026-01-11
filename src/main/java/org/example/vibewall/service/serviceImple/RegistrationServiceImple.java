package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.config.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @Override
    public UsersResponse adduser(UsersRequested request){

        Users user = new Users();
        user.setUsername(encryption.encode(request.getName()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        Users savedUser = repo.save(user);


        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole()
        );


        System.out.println("JWT TOKEN (DEV ONLY): " + token);

        return mapper.toDTO(savedUser);

    }


}
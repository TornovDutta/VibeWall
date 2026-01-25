package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.security.JwtUtil;
import org.example.vibewall.service.RegistrationService;
import org.example.vibewall.utility.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImple implements RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepo repo;
    private final static Logger logger =
            LoggerFactory.getLogger(RegistrationServiceImple.class);
    private final Encryption encryption;
    private final UserMapper mapper;
    private final JwtUtil jwtUtil;


    @Override
    public UsersResponse adduser(UsersRequested request) {

        if (repo.existsByUsername((encryption.encode(request.getName())))){
            throw new RuntimeException("User already exists");
        }

        Users user = new Users();
        user.setUsername(encryption.encode(request.getName()));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        Users savedUser = repo.save(user);


        logger.info("User registered: {}", savedUser.getId());

        return UsersResponse.builder().name(encryption.decode(savedUser.getUsername())).id(user.getId()).build();
    }


    @Override
    public TokenResponse login(UsersRequested request) {

        String encodedUsername = encryption.encode(request.getName());

        Optional<Users> optionalUser = repo.findByUsername(encodedUsername);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        Users user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        logger.info("User logged in: {}", user.getId());
        return TokenResponse.builder().jwt(token).refresh("").build();
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}

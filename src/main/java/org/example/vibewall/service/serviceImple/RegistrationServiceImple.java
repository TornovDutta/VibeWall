package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequestedDTO;
import org.example.vibewall.DTO.UsersResponseDTO;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.RegistrationService;
import org.example.vibewall.utilly.UsersMapper;
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
    private final UsersMapper mapper;

    @Override
    public UsersResponseDTO adduser(UsersRequestedDTO dto) throws UserNotFoundException {

        String hashedPassword = passwordEncoder.encode(dto.password());


        Users user = new Users();
        user.setUsername(encryption.encode(dto.username()));
        user.setPassword(hashedPassword);
        user.setRole("USER");


        Users savedUser = repo.save(user);




        return mapper.toDto(savedUser);
    }


}

package org.example.vibewall.service;

import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.serviceImple.RegistrationServiceImple;
import org.example.vibewall.utility.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UsersRepo repo;
    @Mock private Encryption encryption;
    @Mock private UserMapper mapper;

    @InjectMocks
    private RegistrationServiceImple registrationService;

    @Test
    void adduser_shouldEncryptSaveAndReturnResponse() {
        UsersRequested request = new UsersRequested("tornov", "password123");

        String encryptedUsername = "enc_tornov";
        String encodedPassword   = "enc_pass";
        String userId            = "abc123";

        Users savedUser = new Users();
        savedUser.setId(userId);
        savedUser.setUsername(encryptedUsername);
        savedUser.setPassword(encodedPassword);
        savedUser.setRole("USER");

        UsersResponse response = new UsersResponse(userId, encryptedUsername);

        when(encryption.encode("tornov")).thenReturn(encryptedUsername);
        when(repo.existsByUsername(encryptedUsername)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(repo.save(any(Users.class))).thenReturn(savedUser);
        when(mapper.toDTO(savedUser)).thenReturn(response);

        UsersResponse result = registrationService.adduser(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());

        verify(encryption).encode("tornov");
        verify(passwordEncoder).encode("password123");
        verify(repo).save(any(Users.class));
    }
}

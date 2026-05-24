package org.example.vibewall.service;

import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.service.serviceImpl.RegistrationServiceImpl;
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
    @Mock private UserRepository repo;
    @Mock private Encryption encryption;
    @Mock private UserMapper mapper;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void adduser_shouldEncryptSaveAndReturnResponse() {
        UserRequest request = new UserRequest("tornov", "password123");

        String encryptedUsername = "enc_tornov";
        String encodedPassword   = "enc_pass";
        String userId            = "abc123";

        Users savedUser = new Users();
        savedUser.setId(userId);
        savedUser.setUsername(encryptedUsername);
        savedUser.setPassword(encodedPassword);
        savedUser.setRole("USER");

        when(encryption.encode("tornov")).thenReturn(encryptedUsername);
        when(repo.existsByUsername(encryptedUsername)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(repo.save(any(Users.class))).thenReturn(savedUser);

        UserResponse result = registrationService.adduser(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());

        verify(encryption).encode("tornov");
        verify(passwordEncoder).encode("password123");
        verify(repo).save(any(Users.class));
    }
}

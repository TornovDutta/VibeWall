package org.example.vibewall.service.serviceImple;

import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.config.JwtUtil;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UsersRepo;
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
class RegistrationServiceImpleTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsersRepo repo;

    @Mock
    private Encryption encryption;

    @Mock
    private UserMapper mapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RegistrationServiceImple registrationService;

    @Test
    void adduser_shouldEncryptEncodeSaveAndReturnResponse() {

        // Arrange
        UsersRequested request =
                new UsersRequested("tornov", "password123");

        String encryptedUsername = "enc_tornov";
        String encodedPassword = "enc_pass";
        String userId = "abc123";
        String jwtToken = "mocked_jwt";

        Users savedUser = new Users();
        savedUser.setId(userId);
        savedUser.setUsername(encryptedUsername);
        savedUser.setPassword(encodedPassword);
        savedUser.setRole("USER");

        UsersResponse response =
                new UsersResponse(userId, encryptedUsername);

        when(encryption.encode("tornov")).thenReturn(encryptedUsername);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(repo.save(any(Users.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(userId, encryptedUsername, "USER"))
                .thenReturn(jwtToken);
        when(mapper.toDTO(savedUser)).thenReturn(response);

        // Act
        UsersResponse result = registrationService.adduser(request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals(encryptedUsername, result.name());

        verify(encryption).encode("tornov");
        verify(passwordEncoder).encode("password123");
        verify(repo).save(any(Users.class));
        verify(jwtUtil).generateToken(userId, encryptedUsername, "USER");
        verify(mapper).toDTO(savedUser);
    }
}
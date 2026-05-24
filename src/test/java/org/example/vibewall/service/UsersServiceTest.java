package org.example.vibewall.service;

import org.example.vibewall.DTO.UserRequest;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.service.serviceImpl.UserServiceImpl;
import org.example.vibewall.utility.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository usersRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Encryption encryption;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl usersService;

    private Users existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new Users(
                "1",
                "oldUsername",
                "oldPassword",
                "USER"
        );
    }

    //  UPDATE

    @Test
    void update_shouldUpdateUserSuccessfully() {
        UserRequest request = new UserRequest("newName", "newPassword");

        when(usersRepo.findById("1")).thenReturn(Optional.of(existingUser));
        when(encryption.encode("newName")).thenReturn("encryptedName");
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(usersRepo.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser))
                .thenReturn(new UserResponse("1", "encryptedName"));

        UserResponse response = usersService.update("1", request);

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertEquals("encryptedName", response.getName());

        verify(usersRepo).findById("1");
        verify(encryption).encode("newName");
        verify(passwordEncoder).encode("newPassword");
        verify(usersRepo).save(existingUser);
        verify(userMapper).toDTO(existingUser);
    }

    @Test
    void update_shouldThrowException_whenUserNotFound() {
        UserRequest request = new UserRequest("name", "password");
        when(usersRepo.findById("99")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> usersService.update("99", request)
        );

        verify(usersRepo).findById("99");
        verifyNoInteractions(encryption, passwordEncoder, userMapper);
    }

    //  DELETE

    @Test
    void delete_shouldDeleteUserSuccessfully() throws UserNotFoundException {
        when(usersRepo.findById("1")).thenReturn(Optional.of(existingUser));
        doNothing().when(usersRepo).removeById("1");

        usersService.delete("1");

        verify(usersRepo).findById("1");
        verify(usersRepo).removeById("1");
    }

    @Test
    void delete_shouldThrowException_whenUserNotFound() {
        when(usersRepo.findById("2")).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> usersService.delete("2")
        );

        verify(usersRepo).findById("2");
        verify(usersRepo, never()).removeById(any());
    }

}

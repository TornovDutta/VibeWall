package org.example.vibewall.service;

import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.serviceImple.UsersServiceImplements;
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
    private UsersRepo usersRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Encryption encryption;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UsersServiceImplements usersService;

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
        // given
        UsersRequested request = new UsersRequested("newName", "newPassword");

        when(usersRepo.findById("1")).thenReturn(Optional.of(existingUser));
        when(encryption.encode("newName")).thenReturn("encryptedName");
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(usersRepo.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDTO(existingUser))
                .thenReturn(new UsersResponse("1", "encryptedName"));

        // when
        UsersResponse response = usersService.update("1", request);

        // then
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
        // given
        UsersRequested request = new UsersRequested("name", "password");
        when(usersRepo.findById("99")).thenReturn(Optional.empty());

        // when & then
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
        // given
        when(usersRepo.findById("1")).thenReturn(Optional.of(existingUser));
        doNothing().when(usersRepo).removeById("1");

        // when
        usersService.delete("1");

        // then
        verify(usersRepo).findById("1");
        verify(usersRepo).removeById("1");
    }

    @Test
    void delete_shouldThrowException_whenUserNotFound() {
        // given
        when(usersRepo.findById("2")).thenReturn(Optional.empty());

        // when & then
        assertThrows(
                UserNotFoundException.class,
                () -> usersService.delete("2")
        );

        verify(usersRepo).findById("2");
        verify(usersRepo, never()).removeById(any());
    }

}
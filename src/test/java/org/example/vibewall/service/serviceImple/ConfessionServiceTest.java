package org.example.vibewall.service.serviceImple;

import org.example.vibewall.DTO.ConfessionRequested;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.UnSafeExecption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.OpenAiService;
import org.example.vibewall.utility.ConfessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfessionServiceTest {
    @Mock
    private UsersRepo usersRepo;

    @Mock
    private ConfessionRepo confessionRepo;

    @Mock
    private ConfessionMapper confessionMapper;

    @Mock
    private Encryption encryption;

    @Mock
    private OpenAiService openAiService;

    @InjectMocks
    private ConfessionServiceImplement confessionService;

    private Confession confession;

    @BeforeEach
    void setUp() {
        confession = new Confession();
        confession.setId("c1");
        confession.setUserId("u1");
        confession.setContent("encoded");
    }

    //  CREATE

    @Test
    void create_shouldCreateConfessionSuccessfully() {
        ConfessionRequested request = new ConfessionRequested("hello");

        when(openAiService.unSafe("hello")).thenReturn(false);
        when(encryption.encode("hello")).thenReturn("encoded");
        when(confessionMapper.toDTO(any(Confession.class)))
                .thenReturn(new ConfessionResponse("c1", "encoded", null));

        ConfessionResponse response = confessionService.create("u1", request);

        assertNotNull(response);
        verify(confessionRepo).save(any(Confession.class));
        verify(encryption).encode("hello");
    }

    @Test
    void create_shouldThrowException_whenContentUnsafe() {
        ConfessionRequested request = new ConfessionRequested("bad");

        when(openAiService.unSafe("bad")).thenReturn(true);

        assertThrows(
                UnSafeExecption.class,
                () -> confessionService.create("u1", request)
        );

        verify(confessionRepo, never()).save(any());
    }

    // UPDATE

    @Test
    void update_shouldUpdateConfessionSuccessfully() throws ConfessionNotFoundException {
        ConfessionRequested request = new ConfessionRequested("updated");

        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));
        when(openAiService.unSafe("updated")).thenReturn(false);
        when(encryption.encode("updated")).thenReturn("encodedUpdated");
        when(confessionMapper.toDTO(confession))
                .thenReturn(new ConfessionResponse("c1", "encodedUpdated", null));

        ConfessionResponse response =
                confessionService.update("u1", request, "c1");

        assertNotNull(response);
        assertEquals("encodedUpdated", confession.getContent());
        verify(confessionRepo).save(confession);
    }

    @Test
    void update_shouldThrowException_whenConfessionNotFound() {
        when(confessionRepo.findById("x")).thenReturn(Optional.empty());

        assertThrows(
                ConfessionNotFoundException.class,
                () -> confessionService.update("u1",
                        new ConfessionRequested("a"), "x")
        );
    }

    @Test
    void update_shouldThrowException_whenAccessDenied() {
        confession.setUserId("other");

        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));

        assertThrows(
                AccessDeniedException.class,
                () -> confessionService.update("u1",
                        new ConfessionRequested("a"), "c1")
        );

        verify(confessionRepo, never()).save(any());
    }

    @Test
    void update_shouldThrowException_whenContentUnsafe() {
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));
        when(openAiService.unSafe("bad")).thenReturn(true);

        assertThrows(
                UnSafeExecption.class,
                () -> confessionService.update("u1",
                        new ConfessionRequested("bad"), "c1")
        );
    }

    // DELETE

    @Test
    void delete_shouldDeleteConfessionSuccessfully() throws ConfessionNotFoundException {
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));

        confessionService.delete("u1", "c1");

        verify(confessionRepo).removeById("c1");
    }

    @Test
    void delete_shouldThrowException_whenConfessionNotFound() {
        when(confessionRepo.findById("x")).thenReturn(Optional.empty());

        assertThrows(
                ConfessionNotFoundException.class,
                () -> confessionService.delete("u1", "x")
        );
    }

    @Test
    void delete_shouldThrowException_whenAccessDenied() {
        confession.setUserId("other");
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));

        assertThrows(
                AccessDeniedException.class,
                () -> confessionService.delete("u1", "c1")
        );

        verify(confessionRepo, never()).removeById(any());
    }
}
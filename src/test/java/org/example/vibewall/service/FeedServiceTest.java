package org.example.vibewall.service;

import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.DTO.FeedbackRequested;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.service.serviceImple.FeedbackServiceImplement;
import org.example.vibewall.utility.ConfessionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FeedServiceTest {
    @Mock
    private ConfessionRepo confessionRepo;

    @Mock
    private OpenAiService openAiService;

    @Mock
    private Encryption encryption;

    @Mock
    private ConfessionMapper confessionMapper;

    @InjectMocks
    private FeedbackServiceImplement feedbackService;

    private Confession confession;

    @BeforeEach
    void setUp() {
        confession = new Confession();
        confession.setId("c1");
        confession.setFeedbacks(new ArrayList<>());
    }

    // GIVE FEEDBACK

    @Test
    void giveFeedback_shouldAddFeedbackSuccessfully() throws PrincipalNotFollowException, ConfessionNotFoundException {
        FeedbackRequested request = new FeedbackRequested("good");

        when(openAiService.unSafe("good")).thenReturn(false);
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));
        when(encryption.encode("good")).thenReturn("encodedGood");
        when(confessionMapper.toDTO(confession))
                .thenReturn(mock(ConfessionResponse.class));

        ConfessionResponse response =
                feedbackService.giveFeedback("c1", request);

        assertNotNull(response);
        assertEquals(1, confession.getFeedbacks().size());
        assertEquals(0, confession.getFeedbacks().get(0).getId());
        assertEquals("encodedGood",
                confession.getFeedbacks().get(0).getFeedback());

        verify(confessionRepo).save(confession);
    }

    @Test
    void giveFeedback_shouldThrowException_whenUnsafe() {
        when(openAiService.unSafe("bad")).thenReturn(true);

        assertThrows(
                PrincipalNotFollowException.class,
                () -> feedbackService.giveFeedback(
                        "c1", new FeedbackRequested("bad"))
        );

        verify(confessionRepo, never()).save(any());
    }

    @Test
    void giveFeedback_shouldThrowException_whenConfessionNotFound() {
        when(openAiService.unSafe("ok")).thenReturn(false);
        when(confessionRepo.findById("x")).thenReturn(Optional.empty());

        assertThrows(
                ConfessionNotFoundException.class,
                () -> feedbackService.giveFeedback(
                        "x", new FeedbackRequested("ok"))
        );
    }

    // UPDATE FEEDBACK

    @Test
    void updateFeedback_shouldUpdateSuccessfully() throws PrincipalNotFollowException, ConfessionNotFoundException {
        confession.getFeedbacks().add(new Feedback(0, "old"));

        when(openAiService.unSafe("new")).thenReturn(false);
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));
        when(encryption.encode("new")).thenReturn("encodedNew");
        when(confessionMapper.toDTO(confession))
                .thenReturn(mock(ConfessionResponse.class));

        ConfessionResponse response =
                feedbackService.updateFeedback(
                        "c1", 0, new FeedbackRequested("new"));

        assertNotNull(response);
        assertEquals("encodedNew",
                confession.getFeedbacks().get(0).getFeedback());

        verify(confessionRepo).save(confession);
    }

    @Test
    void updateFeedback_shouldThrowException_whenUnsafe() {
        when(openAiService.unSafe("bad")).thenReturn(true);

        assertThrows(
                PrincipalNotFollowException.class,
                () -> feedbackService.updateFeedback(
                        "c1", 0, new FeedbackRequested("bad"))
        );
    }

    @Test
    void updateFeedback_shouldThrowException_whenFeedbackNotFound() {
        when(openAiService.unSafe("ok")).thenReturn(false);
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));

        assertThrows(
                ConfessionNotFoundException.class,
                () -> feedbackService.updateFeedback(
                        "c1", 0, new FeedbackRequested("ok"))
        );
    }

    // DELETE FEEDBACK

    @Test
    void deleteFeedback_shouldDeleteAndReindexSuccessfully() throws ConfessionNotFoundException {
        confession.getFeedbacks().add(new Feedback(0, "f1"));
        confession.getFeedbacks().add(new Feedback(1, "f2"));

        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));
        when(confessionMapper.toDTO(confession))
                .thenReturn(mock(ConfessionResponse.class));

        ConfessionResponse response =
                feedbackService.deleteFeedback("c1", 0);

        assertNotNull(response);
        assertEquals(1, confession.getFeedbacks().size());
        assertEquals(0, confession.getFeedbacks().get(0).getId());

        verify(confessionRepo).save(confession);
    }

    @Test
    void deleteFeedback_shouldThrowException_whenFeedbackNotFound() {
        when(confessionRepo.findById("c1")).thenReturn(Optional.of(confession));

        assertThrows(
                ConfessionNotFoundException.class,
                () -> feedbackService.deleteFeedback("c1", 0)
        );
    }

    @Test
    void deleteFeedback_shouldThrowException_whenConfessionNotFound() {
        when(confessionRepo.findById("x")).thenReturn(Optional.empty());

        assertThrows(
                ConfessionNotFoundException.class,
                () -> feedbackService.deleteFeedback("x", 0)
        );
    }

}
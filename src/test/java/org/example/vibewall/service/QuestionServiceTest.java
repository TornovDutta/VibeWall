package org.example.vibewall.service;

import org.example.vibewall.DTO.QuestionRequest;
import org.example.vibewall.DTO.QuestionResponse;
import org.example.vibewall.DTO.ReplyQuestionRequest;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.UnsafeContentException;
import org.example.vibewall.model.Question;
import org.example.vibewall.repo.QuestionRepository;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.serviceImpl.QuestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AiService aiService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReputationService reputationService;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question sampleQuestion;

    @BeforeEach
    void setUp() {
        sampleQuestion = Question.builder()
                .id("q1")
                .targetUserId("target123")
                .askerUserId("asker456")
                .askerAlias("Cosmic Fox")
                .isAnonymous(false)
                .content("What is your biggest fear?")
                .status("PENDING")
                .createdAt(new Date())
                .build();
    }

    @Test
    void askQuestion_anonymous_success() {
        QuestionRequest request = QuestionRequest.builder()
                .content("Do you like coding?")
                .anonymous(true)
                .build();

        when(userRepo.existsById("target123")).thenReturn(true);
        when(aiService.unSafe("Do you like coding?")).thenReturn(false);
        when(questionRepo.save(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId("q_saved");
            return q;
        });

        QuestionResponse response = questionService.askQuestion("target123", request, null);

        assertNotNull(response);
        assertEquals("q_saved", response.getId());
        assertEquals("Anonymous", response.getAskerAlias());
        assertTrue(response.isAnonymous());
        assertEquals("PENDING", response.getStatus());

        verify(notificationService).notifyQuestionReceived(eq("target123"), eq("q_saved"), eq("Do you like coding?"));
    }

    @Test
    void askQuestion_withIdentity_authenticatedUser() {
        QuestionRequest request = QuestionRequest.builder()
                .content("Favorite book?")
                .anonymous(false)
                .build();

        CustomUserDetails user = new CustomUserDetails("asker456", "john", "USER");

        when(userRepo.existsById("target123")).thenReturn(true);
        when(aiService.unSafe("Favorite book?")).thenReturn(false);
        when(questionRepo.save(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId("q_saved2");
            return q;
        });

        QuestionResponse response = questionService.askQuestion("target123", request, user);

        assertNotNull(response);
        assertFalse(response.isAnonymous());
        assertNotEquals("Anonymous", response.getAskerAlias());
    }

    @Test
    void askQuestion_unsafe_throwsException() {
        QuestionRequest request = QuestionRequest.builder()
                .content("toxic hate content")
                .anonymous(true)
                .build();

        when(userRepo.existsById("target123")).thenReturn(true);
        when(aiService.unSafe("toxic hate content")).thenReturn(true);

        assertThrows(UnsafeContentException.class, () ->
                questionService.askQuestion("target123", request, null)
        );
    }

    @Test
    void replyQuestion_publicly_success() {
        ReplyQuestionRequest request = ReplyQuestionRequest.builder()
                .answer("Spiders!")
                .isPublic(true)
                .build();

        when(questionRepo.findById("q1")).thenReturn(Optional.of(sampleQuestion));
        when(aiService.unSafe("Spiders!")).thenReturn(false);
        when(questionRepo.save(any(Question.class))).thenAnswer(i -> i.getArgument(0));

        QuestionResponse response = questionService.replyQuestion("target123", "q1", request);

        assertNotNull(response);
        assertEquals("ANSWERED", response.getStatus());
        assertEquals("Spiders!", response.getAnswer());
        assertTrue(response.isPublic());

        verify(notificationService).notifyQuestionAnswered(eq("asker456"), eq("q1"), eq("Spiders!"), eq(true));
    }

    @Test
    void replyQuestion_unauthorized_throwsAccessDenied() {
        ReplyQuestionRequest request = ReplyQuestionRequest.builder()
                .answer("Not my question")
                .isPublic(true)
                .build();

        when(questionRepo.findById("q1")).thenReturn(Optional.of(sampleQuestion));

        assertThrows(AccessDeniedException.class, () ->
                questionService.replyQuestion("wrongUser", "q1", request)
        );
    }

    @Test
    void deleteQuestion_owner_success() {
        when(questionRepo.findById("q1")).thenReturn(Optional.of(sampleQuestion));

        questionService.deleteQuestion("target123", "q1", false);

        verify(questionRepo).deleteById("q1");
    }

    @Test
    void getPublicWallQuestions_returnsOnlyPublicAnswered() {
        sampleQuestion.setStatus("ANSWERED");
        sampleQuestion.setPublic(true);

        when(questionRepo.findByTargetUserIdAndStatusAndIsPublicTrueOrderByAnsweredAtDesc("target123", "ANSWERED"))
                .thenReturn(List.of(sampleQuestion));

        List<QuestionResponse> results = questionService.getPublicWallQuestions("target123");

        assertEquals(1, results.size());
        assertEquals("q1", results.get(0).getId());
    }

    @Test
    void askQuestion_community_success() {
        QuestionRequest request = QuestionRequest.builder()
                .content("What is the meaning of life?")
                .anonymous(true)
                .build();

        when(aiService.unSafe("What is the meaning of life?")).thenReturn(false);
        when(questionRepo.save(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId("q_community");
            return q;
        });

        QuestionResponse response = questionService.askQuestion("community", request, null);

        assertNotNull(response);
        assertEquals("q_community", response.getId());
        assertEquals("Community", response.getTargetAlias());
        assertTrue(response.isPublic());
        verify(notificationService, never()).notifyQuestionReceived(any(), any(), any());
    }

    @Test
    void getFeedQuestions_returnsList() {
        sampleQuestion.setPublic(true);
        when(questionRepo.findAllPublicQuestions(any())).thenReturn(List.of(sampleQuestion));

        List<QuestionResponse> results = questionService.getFeedQuestions();

        assertEquals(1, results.size());
        assertEquals("q1", results.get(0).getId());
    }
}

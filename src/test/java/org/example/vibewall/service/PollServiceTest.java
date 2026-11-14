package org.example.vibewall.service;

import org.example.vibewall.DTO.CreatePollRequest;
import org.example.vibewall.DTO.PollResponse;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.model.Poll;
import org.example.vibewall.model.PollOption;
import org.example.vibewall.repo.PollRepository;
import org.example.vibewall.service.serviceImpl.PollServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {

    @Mock
    private PollRepository pollRepo;

    @Mock
    private AiService aiService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReputationService reputationService;

    @InjectMocks
    private PollServiceImpl pollService;

    private Poll poll;

    @BeforeEach
    void setUp() {
        poll = Poll.builder()
                .id("poll123")
                .question("Do you believe in soulmates?")
                .category("Relationships")
                .userId("author1")
                .options(new ArrayList<>(List.of(
                        new PollOption(0, "Yes, absolutely", 5),
                        new PollOption(1, "No, it's just luck", 5)
                )))
                .votedUserIds(new HashSet<>(List.of("userA")))
                .userVotes(new HashMap<>(Map.of("userA", 0)))
                .totalVotes(10)
                .createdAt(new Date())
                .expiresAt(new Date(System.currentTimeMillis() + 1000000))
                .build();
    }

    @Test
    void createPoll_shouldSavePollAndNotifyAuthor() {
        CreatePollRequest req = CreatePollRequest.builder()
                .question("Is pineapple on pizza acceptable?")
                .category("Food")
                .options(List.of("Yes", "Never"))
                .build();

        when(aiService.unSafe(any())).thenReturn(false);
        when(pollRepo.save(any(Poll.class))).thenAnswer(i -> {
            Poll p = i.getArgument(0);
            p.setId("p_generated");
            return p;
        });

        PollResponse res = pollService.createPoll("user123", req);

        assertNotNull(res);
        assertEquals("p_generated", res.getId());
        assertEquals("Is pineapple on pizza acceptable?", res.getQuestion());
        assertEquals(2, res.getOptions().size());
        verify(notificationService).notifyPollCreated(eq("user123"), eq("p_generated"), any());
    }

    @Test
    void vote_shouldIncrementVoteCountAndCalculatePercentages() {
        when(pollRepo.findById("poll123")).thenReturn(Optional.of(poll));
        when(pollRepo.save(any(Poll.class))).thenAnswer(i -> i.getArgument(0));

        PollResponse res = pollService.vote("poll123", 1, "userNew");

        assertEquals(11, res.getTotalVotes());
        assertEquals(6, res.getOptions().get(1).getVoteCount());
        assertEquals(1, res.getUserVotedOptionId());
        verify(notificationService).notifyPollVote(eq("poll123"), eq(poll.getQuestion()), eq("author1"), eq(11));
    }

    @Test
    void vote_shouldAllowSwitchingVote() {
        when(pollRepo.findById("poll123")).thenReturn(Optional.of(poll));
        when(pollRepo.save(any(Poll.class))).thenAnswer(i -> i.getArgument(0));

        // userA previously voted option 0 (count 5). Now switches to option 1 (count 5).
        PollResponse res = pollService.vote("poll123", 1, "userA");

        // Total votes shouldn't increase on a switch
        assertEquals(10, res.getTotalVotes());
        assertEquals(4, res.getOptions().get(0).getVoteCount());
        assertEquals(6, res.getOptions().get(1).getVoteCount());
        assertEquals(1, res.getUserVotedOptionId());
    }

    @Test
    void deletePoll_byAuthorOrAdmin_shouldSucceed() {
        when(pollRepo.findById("poll123")).thenReturn(Optional.of(poll));

        pollService.deletePoll("poll123", "author1", false);
        verify(pollRepo).deleteById("poll123");
    }

    @Test
    void deletePoll_byNonAuthor_shouldThrowAccessDenied() {
        when(pollRepo.findById("poll123")).thenReturn(Optional.of(poll));

        assertThrows(AccessDeniedException.class, () -> {
            pollService.deletePoll("poll123", "stranger", false);
        });
    }
}

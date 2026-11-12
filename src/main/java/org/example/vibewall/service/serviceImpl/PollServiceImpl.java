package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.CreatePollRequest;
import org.example.vibewall.DTO.PollOptionResponse;
import org.example.vibewall.DTO.PollResponse;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.PollNotFoundException;
import org.example.vibewall.exception.UnsafeContentException;
import org.example.vibewall.model.Poll;
import org.example.vibewall.model.PollOption;
import org.example.vibewall.repo.PollRepository;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.NotificationService;
import org.example.vibewall.service.PollService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {

    private final PollRepository pollRepo;
    private final AiService aiService;
    private final NotificationService notificationService;

    private static final long POLL_TTL_MILLIS = 24L * 3600 * 1000; // 24 hours live duration

    @Override
    public PollResponse createPoll(String userId, CreatePollRequest request) {
        if (aiService.unSafe(request.getQuestion())) {
            throw new UnsafeContentException("Content flagged by AI moderation: violates community safety guidelines.");
        }

        List<PollOption> optionList = new ArrayList<>();
        int index = 0;
        for (String optText : request.getOptions()) {
            if (optText != null && !optText.trim().isEmpty()) {
                optionList.add(PollOption.builder()
                        .id(index++)
                        .text(optText.trim())
                        .voteCount(0)
                        .build());
            }
        }

        if (optionList.size() < 2) {
            throw new IllegalArgumentException("Poll must have at least 2 valid options.");
        }

        long now = System.currentTimeMillis();
        Poll poll = Poll.builder()
                .question(request.getQuestion().trim())
                .category(request.getCategory() != null && !request.getCategory().isBlank()
                        ? request.getCategory().trim()
                        : "General")
                .userId(userId)
                .options(optionList)
                .votedUserIds(new HashSet<>())
                .userVotes(new HashMap<>())
                .totalVotes(0)
                .createdAt(new Date(now))
                .expiresAt(new Date(now + POLL_TTL_MILLIS))
                .build();

        Poll saved = pollRepo.save(poll);

        // Notify creator
        notificationService.notifyPollCreated(userId, saved.getId(), saved.getQuestion());

        return toResponse(saved, userId);
    }

    @Override
    public List<PollResponse> getFeedPolls(String currentUserId) {
        return pollRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> toResponse(p, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public PollResponse getPollById(String pollId, String currentUserId) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found with id: " + pollId));
        return toResponse(poll, currentUserId);
    }

    @Override
    public PollResponse vote(String pollId, int optionId, String currentUserId) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found with id: " + pollId));

        if (!poll.isActive()) {
            throw new IllegalStateException("This poll has ended and is no longer accepting votes.");
        }

        if (optionId < 0 || optionId >= poll.getOptions().size()) {
            throw new IllegalArgumentException("Invalid option selected: " + optionId);
        }

        if (currentUserId == null || currentUserId.isBlank()) {
            throw new AccessDeniedException("You must be logged in to vote.");
        }

        if (poll.getUserVotes() == null) {
            poll.setUserVotes(new HashMap<>());
        }
        if (poll.getVotedUserIds() == null) {
            poll.setVotedUserIds(new HashSet<>());
        }

        Integer previousOptionId = poll.getUserVotes().get(currentUserId);

        if (previousOptionId != null) {
            // User already voted this exact option
            if (previousOptionId == optionId) {
                return toResponse(poll, currentUserId);
            }
            // User is switching their vote: decrement previous option
            PollOption prevOpt = poll.getOptions().get(previousOptionId);
            prevOpt.setVoteCount(Math.max(0, prevOpt.getVoteCount() - 1));
        } else {
            // First time voting
            poll.setTotalVotes(poll.getTotalVotes() + 1);
            poll.getVotedUserIds().add(currentUserId);
        }

        // Increment selected option
        PollOption chosenOpt = poll.getOptions().get(optionId);
        chosenOpt.setVoteCount(chosenOpt.getVoteCount() + 1);
        poll.getUserVotes().put(currentUserId, optionId);

        Poll saved = pollRepo.save(poll);

        // Notify poll owner if voter is not the owner
        if (!currentUserId.equals(poll.getUserId())) {
            notificationService.notifyPollVote(saved.getId(), saved.getQuestion(), saved.getUserId(), saved.getTotalVotes());
        }

        return toResponse(saved, currentUserId);
    }

    @Override
    public void deletePoll(String pollId, String currentUserId, boolean isAdmin) {
        Poll poll = pollRepo.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException("Poll not found with id: " + pollId));

        if (!isAdmin && (poll.getUserId() == null || !poll.getUserId().equals(currentUserId))) {
            throw new AccessDeniedException("You are not authorized to delete this poll.");
        }

        pollRepo.deleteById(pollId);
    }

    private PollResponse toResponse(Poll poll, String currentUserId) {
        int total = poll.getTotalVotes();
        List<PollOptionResponse> optionResponses = poll.getOptions().stream().map(opt -> {
            double pct = total == 0 ? 0.0 : Math.round(((double) opt.getVoteCount() / total) * 1000.0) / 10.0;
            return PollOptionResponse.builder()
                    .id(opt.getId())
                    .text(opt.getText())
                    .voteCount(opt.getVoteCount())
                    .percentage(pct)
                    .build();
        }).collect(Collectors.toList());

        Integer userVotedOptId = null;
        if (currentUserId != null && poll.getUserVotes() != null) {
            userVotedOptId = poll.getUserVotes().get(currentUserId);
        }

        return PollResponse.builder()
                .id(poll.getId())
                .question(poll.getQuestion())
                .category(poll.getCategory())
                .userId(poll.getUserId())
                .options(optionResponses)
                .totalVotes(total)
                .userVotedOptionId(userVotedOptId)
                .active(poll.isActive())
                .createdAt(poll.getCreatedAt())
                .expiresAt(poll.getExpiresAt())
                .build();
    }
}

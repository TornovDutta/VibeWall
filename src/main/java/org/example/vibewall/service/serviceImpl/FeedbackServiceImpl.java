package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.DTO.FeedbackRequest;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PlatformMisuseException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.service.FeedbackService;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.NotificationService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
    private final ConfessionRepository confessionRepo;
    private final AiService aiService;
    private final Encryption encryption;
    private final ConfessionMapper mapper;
    private final NotificationService notificationService;
    private final org.example.vibewall.service.ReputationService reputationService;

    @Override
    @CacheEvict(
            value = {"confession-feed", "confession-by-id"},
            allEntries = true
    )
    public ConfessionResponse updateFeedback(
            String confessionId,
            int feedbackId,
            FeedbackRequest requested)
            throws ConfessionNotFoundException, PlatformMisuseException {

        if (aiService.unSafe(requested.getContent())) {
            throw new PlatformMisuseException("Content flagged by AI safety moderation: violates community guidelines (harassment, hate speech, or offensive content).");
        }

        Confession confession = confessionRepo.findById(confessionId)
                .orElseThrow(() -> new ConfessionNotFoundException("wrong id"));

        if (feedbackId < 0 || feedbackId >= confession.getFeedbacks().size()) {
            throw new ConfessionNotFoundException("feedback not found");
        }

        Feedback feedback = confession.getFeedbacks().get(feedbackId);
        feedback.setContent(encryption.encode(requested.getContent()));

        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse giveFeedback(String confessionId, FeedbackRequest requested)
            throws ConfessionNotFoundException, PlatformMisuseException {
        return giveFeedback(null, confessionId, requested);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse giveFeedback(String userId, String confessionId, FeedbackRequest requested)
            throws ConfessionNotFoundException, PlatformMisuseException {

        if (userId != null && reputationService.isUserBanned(userId)) {
            throw new org.example.vibewall.exception.AccessDeniedException("Your account is currently suspended due to negative reputation points. Contact admin to appeal.");
        }

        if (aiService.unSafe(requested.getContent())) {
            if (userId != null) {
                reputationService.penalizeHateViolation(userId, "Hateful comment on confession", 25);
            }
            throw new PlatformMisuseException("Comment flagged by AI safety moderation: Hateful, abusive, or violating content detected. 25 points have been deducted from your reputation score.");
        }

        Confession confession = confessionRepo.findById(confessionId).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        int feedbackId = confession.getFeedbacks().size();

        Feedback feedback = new Feedback(feedbackId, encryption.encode(requested.getContent()), userId);

        confession.getFeedbacks().add(feedback);
        confessionRepo.save(confession);

        notificationService.notifyFeedback(confessionId, requested.getContent());

        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse likeFeedback(String confessionId, int feedbackId, String voterUserId)
            throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(confessionId).orElseThrow(() ->
                new ConfessionNotFoundException("wrong id"));

        if (feedbackId < 0 || feedbackId >= confession.getFeedbacks().size()) {
            throw new ConfessionNotFoundException("feedback not found");
        }

        Feedback feedback = confession.getFeedbacks().get(feedbackId);
        if (feedback.getLikedUserIds() == null) {
            feedback.setLikedUserIds(new java.util.HashSet<>());
        }

        if (voterUserId != null && !feedback.getLikedUserIds().contains(voterUserId)) {
            feedback.getLikedUserIds().add(voterUserId);
            feedback.setLikes(feedback.getLikes() + 1);

            // Award points to the author of the comment if not self-like
            if (feedback.getUserId() != null && !feedback.getUserId().equals(voterUserId)) {
                reputationService.awardPoints(feedback.getUserId(), 5, "Received like on comment");
            }

            confessionRepo.save(confession);
        }

        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse deleteFeedback(
            String confessionId,
            int feedbackId)
            throws ConfessionNotFoundException {

        Confession confession = confessionRepo.findById(confessionId)
                .orElseThrow(() -> new ConfessionNotFoundException("wrong id"));

        if (feedbackId < 0 || feedbackId >= confession.getFeedbacks().size()) {
            throw new ConfessionNotFoundException("feedback not found");
        }

        confession.getFeedbacks().remove(feedbackId);

        for (int i = 0; i < confession.getFeedbacks().size(); i++) {
            confession.getFeedbacks().get(i).setId(i);
        }

        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }

}

package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.NotificationResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Notification;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.repo.NotificationRepository;
import org.example.vibewall.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepo;
    private final ConfessionRepository confessionRepo;
    private final Encryption encryption;

    @Override
    public void notifyConfessionCreated(String userId, String confessionId, String rawContent) {
        if (userId == null || userId.isBlank()) return;
        try {
            String snippet = rawContent != null && rawContent.length() > 40
                    ? rawContent.substring(0, 40) + "..."
                    : (rawContent != null ? rawContent : "Confession");

            Notification notification = Notification.builder()
                    .userId(userId)
                    .confessionId(confessionId)
                    .type("POST_CREATED")
                    .title("Confession Published")
                    .message("Your confession is now live: \"" + snippet + "\". It will expire in 12 hours.")
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepo.save(notification);
        } catch (Exception e) {
            logger.warn("Failed to create post notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyReaction(String confessionId, String reactionType) {
        if (confessionId == null || reactionType == null) return;
        try {
            Confession confession = confessionRepo.findById(confessionId).orElse(null);
            if (confession == null || confession.getUserId() == null || confession.getUserId().isBlank()) {
                return;
            }

            String decoded = "your confession";
            try {
                if (confession.getContent() != null) {
                    String plain = encryption.decode(confession.getContent());
                    decoded = plain.length() > 35 ? plain.substring(0, 35) + "..." : plain;
                }
            } catch (Exception ignored) {}

            String emoji = switch (reactionType.toLowerCase()) {
                case "heart" -> "❤️";
                case "hug" -> "🤗";
                case "relate" -> "✨";
                case "strength" -> "💪";
                default -> "👍";
            };

            Notification notification = Notification.builder()
                    .userId(confession.getUserId())
                    .confessionId(confessionId)
                    .type("REACTION")
                    .title("New Reaction " + emoji)
                    .message("Someone gave a " + reactionType + " " + emoji + " to: \"" + decoded + "\"")
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepo.save(notification);
        } catch (Exception e) {
            logger.warn("Failed to create reaction notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyFeedback(String confessionId, String feedbackContent) {
        if (confessionId == null) return;
        try {
            Confession confession = confessionRepo.findById(confessionId).orElse(null);
            if (confession == null || confession.getUserId() == null || confession.getUserId().isBlank()) {
                return;
            }

            String commentSnippet = feedbackContent != null && feedbackContent.length() > 40
                    ? feedbackContent.substring(0, 40) + "..."
                    : (feedbackContent != null ? feedbackContent : "");

            Notification notification = Notification.builder()
                    .userId(confession.getUserId())
                    .confessionId(confessionId)
                    .type("COMMENT")
                    .title("New Comment")
                    .message("Someone commented: \"" + commentSnippet + "\" on your confession.")
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepo.save(notification);
        } catch (Exception e) {
            logger.warn("Failed to create feedback notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyPollCreated(String userId, String pollId, String question) {
        if (userId == null || userId.isBlank()) return;
        try {
            String snippet = question != null && question.length() > 45
                    ? question.substring(0, 45) + "..."
                    : (question != null ? question : "Poll");

            Notification notification = Notification.builder()
                    .userId(userId)
                    .confessionId(pollId)
                    .type("POLL_CREATED")
                    .title("Poll Launched 🗳️")
                    .message("Your poll is live: \"" + snippet + "\". Votes and opinions are now open!")
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepo.save(notification);
        } catch (Exception e) {
            logger.warn("Failed to create poll notification: {}", e.getMessage());
        }
    }

    @Override
    public void notifyPollVote(String pollId, String pollQuestion, String pollOwnerUserId, int totalVotes) {
        if (pollOwnerUserId == null || pollOwnerUserId.isBlank()) return;
        try {
            String snippet = pollQuestion != null && pollQuestion.length() > 35
                    ? pollQuestion.substring(0, 35) + "..."
                    : (pollQuestion != null ? pollQuestion : "Poll");

            Notification notification = Notification.builder()
                    .userId(pollOwnerUserId)
                    .confessionId(pollId)
                    .type("POLL_VOTE")
                    .title("New Poll Vote 🗳️")
                    .message("Someone cast a vote on: \"" + snippet + "\" (" + totalVotes + " total votes)")
                    .read(false)
                    .createdAt(new Date())
                    .build();

            notificationRepo.save(notification);
        } catch (Exception e) {
            logger.warn("Failed to create poll vote notification: {}", e.getMessage());
        }
    }

    @Override
    public List<NotificationResponse> getUserNotifications(String userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

    @Override
    public NotificationResponse markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepo.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setRead(true);
            notificationRepo.save(notification);
            return toResponse(notification);
        }
        return null;
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> list = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepo.save(n);
            }
        }
    }

    @Override
    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepo.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationRepo.deleteById(notificationId);
        }
    }

    @Override
    public void clearAllNotifications(String userId) {
        notificationRepo.deleteByUserId(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .confessionId(n.getConfessionId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

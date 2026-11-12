package org.example.vibewall.service;

import org.example.vibewall.DTO.NotificationResponse;

import java.util.List;

public interface NotificationService {
    void notifyConfessionCreated(String userId, String confessionId, String snippet);
    void notifyReaction(String confessionId, String reactionType);
    void notifyFeedback(String confessionId, String feedbackContent);
    void notifyPollCreated(String userId, String pollId, String question);
    void notifyPollVote(String pollId, String pollQuestion, String pollOwnerUserId, int totalVotes);
    void notifyQuestionReceived(String targetUserId, String questionId, String questionSnippet);
    void notifyQuestionAnswered(String askerUserId, String questionId, String answerSnippet, boolean isPublic);
    List<NotificationResponse> getUserNotifications(String userId);
    long getUnreadCount(String userId);
    NotificationResponse markAsRead(String userId, String notificationId);
    void markAllAsRead(String userId);
    void deleteNotification(String userId, String notificationId);
    void clearAllNotifications(String userId);
}

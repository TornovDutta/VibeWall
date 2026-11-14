package org.example.vibewall.service;

public interface ReputationService {

    /**
     * Decreases points when a user attempts to post hateful or abusive content.
     * If points drop below zero, the user account is automatically banned.
     */
    void penalizeHateViolation(String userId, String reason, int penalty);

    /**
     * Increases points when other community members like or react positively to user's content.
     */
    void awardPoints(String targetUserId, int pointsAwarded, String reason);

    /**
     * Unbans a user and restores their points to a positive standing.
     */
    void unbanUser(String userId, int restorePoints);

    /**
     * Manually bans a user.
     */
    void banUser(String userId, String reason);

    /**
     * Checks if a user is currently banned.
     */
    boolean isUserBanned(String userId);

    /**
     * Gets current reputation points for a user.
     */
    int getUserPoints(String userId);
}

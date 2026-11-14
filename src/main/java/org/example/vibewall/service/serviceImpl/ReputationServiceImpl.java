package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.model.Users;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.service.ReputationService;
import org.example.vibewall.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReputationServiceImpl implements ReputationService {

    private static final Logger logger = LoggerFactory.getLogger(ReputationServiceImpl.class);

    private final UserRepository userRepo;
    private final SystemLogService systemLogService;

    @Override
    public void penalizeHateViolation(String userId, String reason, int penalty) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        userRepo.findById(userId).ifPresent(user -> {
            int currentPoints = user.getPoints();
            int newPoints = currentPoints - penalty;
            user.setPoints(newPoints);

            logger.warn("User {} penalized by -{} points for violation: '{}'. New score: {}",
                    userId, penalty, reason, newPoints);

            // Automatic ban if points drop below zero
            if (newPoints < 0 && !user.isBanned()) {
                user.setBanned(true);
                user.setBannedAt(new Date());
                String banDesc = "Auto-banned: Points dropped to " + newPoints + " due to hate speech/abusive content violation: " + reason;
                user.setBanReason(banDesc);
                logger.error("USER {} AUTOMATICALLY BANNED: negative points ({})", userId, newPoints);

                if (systemLogService != null) {
                    systemLogService.recordLog(
                            "CRITICAL",
                            "SECURITY_BAN",
                            "REPUTATION_SYSTEM",
                            403,
                            userId,
                            "User banned automatically: Reputation points dropped to " + newPoints,
                            banDesc
                    );
                }
            } else if (systemLogService != null) {
                systemLogService.recordLog(
                        "WARN",
                        "REPUTATION",
                        "CONTENT_MODERATION",
                        422,
                        userId,
                        "Reputation penalized (-" + penalty + " pts) for hateful content",
                        reason + ". Remaining points: " + newPoints
                );
            }

            userRepo.save(user);
        });
    }

    @Override
    public void awardPoints(String targetUserId, int pointsAwarded, String reason) {
        if (targetUserId == null || targetUserId.isBlank()) {
            return;
        }

        userRepo.findById(targetUserId).ifPresent(user -> {
            // Do not award points to banned users
            if (user.isBanned()) {
                return;
            }
            int newPoints = user.getPoints() + pointsAwarded;
            user.setPoints(newPoints);
            userRepo.save(user);
            logger.info("User {} awarded +{} points for '{}'. Total: {}", targetUserId, pointsAwarded, reason, newPoints);
        });
    }

    @Override
    public void unbanUser(String userId, int restorePoints) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        userRepo.findById(userId).ifPresent(user -> {
            user.setBanned(false);
            user.setBanReason(null);
            user.setBannedAt(null);
            user.setPoints(Math.max(restorePoints, 50)); // Ensure at least 50 points upon unban
            userRepo.save(user);
            logger.info("User {} unbanned by administrator. Points restored to {}", userId, user.getPoints());

            if (systemLogService != null) {
                systemLogService.recordLog(
                        "INFO",
                        "ADMIN_ACTION",
                        "/admin/users/unban",
                        200,
                        userId,
                        "User account unbanned by admin",
                        "Points restored to " + user.getPoints()
                );
            }
        });
    }

    @Override
    public void banUser(String userId, String reason) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        userRepo.findById(userId).ifPresent(user -> {
            user.setBanned(true);
            user.setBanReason(reason != null && !reason.isBlank() ? reason : "Manually banned by administrator");
            user.setBannedAt(new Date());
            userRepo.save(user);
            logger.warn("User {} manually banned: {}", userId, user.getBanReason());

            if (systemLogService != null) {
                systemLogService.recordLog(
                        "WARN",
                        "ADMIN_ACTION",
                        "/admin/users/ban",
                        200,
                        userId,
                        "User account manually suspended by admin",
                        user.getBanReason()
                );
            }
        });
    }

    @Override
    public boolean isUserBanned(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return userRepo.findById(userId)
                .map(Users::isBanned)
                .orElse(false);
    }

    @Override
    public int getUserPoints(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0;
        }
        return userRepo.findById(userId)
                .map(Users::getPoints)
                .orElse(0);
    }
}

package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionRequest;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.UnsafeContentException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.service.ConfessionService;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.NotificationService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfessionServiceImpl implements ConfessionService {
    private final UserRepository usersRepo;
    private final ConfessionRepository confessionRepo;
    private final ConfessionMapper mapper;
    private final Encryption encryption;
    private final AiService aiService;
    private final NotificationService notificationService;
    private final org.example.vibewall.service.ReputationService reputationService;

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse create(String userId, ConfessionRequest requested) {

        if (userId != null && reputationService.isUserBanned(userId)) {
            throw new AccessDeniedException("Your account is currently suspended due to negative reputation points. Please contact an administrator to appeal.");
        }

        if (aiService.unSafe(requested.getContent())) {
            if (userId != null) {
                reputationService.penalizeHateViolation(userId, "Hateful confession submission", 25);
            }
            throw new UnsafeContentException("Content flagged by AI safety moderation: Hateful, abusive, or violating content detected. 25 points have been deducted from your reputation score.");
        }

        Confession confession = new Confession(encryption.encode(requested.getContent()), userId);

        Confession saved = confessionRepo.save(confession);
        if (saved == null) {
            saved = confession;
        }

        notificationService.notifyConfessionCreated(userId, saved.getId(), requested.getContent());

        return mapper.toDTO(saved);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse update(String userId, ConfessionRequest requested, String id) throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if (!confession.getUserId().equals(userId)) {
            throw new AccessDeniedException("access denied");
        }
        if (userId != null && reputationService.isUserBanned(userId)) {
            throw new AccessDeniedException("Your account is currently suspended due to negative reputation points. Please contact an administrator to appeal.");
        }
        if (aiService.unSafe(requested.getContent())) {
            if (userId != null) {
                reputationService.penalizeHateViolation(userId, "Hateful confession update", 25);
            }
            throw new UnsafeContentException("Content flagged by AI safety moderation: Hateful, abusive, or violating content detected. 25 points have been deducted from your reputation score.");
        }
        confession.setContent(encryption.encode(requested.getContent()));
        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public void delete(String userId, String id) throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if (!confession.getUserId().equals(userId)) {
            throw new AccessDeniedException("access denied");
        }
        confessionRepo.removeById(id);
    }

}

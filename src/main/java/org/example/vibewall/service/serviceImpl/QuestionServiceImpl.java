package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.QuestionRequest;
import org.example.vibewall.DTO.QuestionResponse;
import org.example.vibewall.DTO.ReplyQuestionRequest;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.UnsafeContentException;
import org.example.vibewall.model.Question;
import org.example.vibewall.repo.QuestionRepository;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.NotificationService;
import org.example.vibewall.service.QuestionService;
import org.example.vibewall.utility.AnonNameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionServiceImpl.class);

    private final QuestionRepository questionRepo;
    private final UserRepository userRepo;
    private final AiService aiService;
    private final NotificationService notificationService;
    private final org.example.vibewall.service.ReputationService reputationService;

    @Override
    public QuestionResponse askQuestion(String targetUserId, QuestionRequest request, CustomUserDetails currentUser) {
        if (currentUser != null && reputationService.isUserBanned(currentUser.getId())) {
            throw new AccessDeniedException("Your account is currently suspended due to negative reputation points. Contact admin to appeal.");
        }

        if (aiService.unSafe(request.getContent())) {
            if (currentUser != null) {
                reputationService.penalizeHateViolation(currentUser.getId(), "Hateful Q&A question submission", 25);
            }
            throw new UnsafeContentException("Question flagged by AI moderation: violates community safety guidelines. 25 points deducted from reputation score.");
        }

        boolean isCommunity = targetUserId == null || targetUserId.isBlank() || "community".equalsIgnoreCase(targetUserId);
        String finalTargetUserId = isCommunity ? "community" : targetUserId.trim();

        if (!isCommunity && !userRepo.existsById(finalTargetUserId)) {
            throw new IllegalArgumentException("User wall not found");
        }

        String askerUserId = null;
        String askerAlias;
        boolean isAnonymous = request.isAnonymous();

        if (isAnonymous) {
            askerAlias = "Anonymous";
        } else {
            if (currentUser != null && currentUser.getId() != null) {
                askerUserId = currentUser.getId();
                askerAlias = AnonNameUtil.generate(currentUser.getId());
            } else if (request.getAskerName() != null && !request.getAskerName().isBlank()) {
                askerAlias = request.getAskerName().trim();
            } else {
                askerAlias = "Anonymous";
                isAnonymous = true;
            }
        }

        Question question = Question.builder()
                .targetUserId(finalTargetUserId)
                .askerUserId(askerUserId)
                .askerAlias(askerAlias)
                .isAnonymous(isAnonymous)
                .content(request.getContent().trim())
                .status("PENDING")
                .isPublic(true)
                .createdAt(new Date())
                .build();

        Question saved = questionRepo.save(question);
        if (saved == null) {
            saved = question;
        }

        if (!isCommunity) {
            notificationService.notifyQuestionReceived(finalTargetUserId, saved.getId(), saved.getContent());
        }
        logger.info("New question asked to targetUserId={}, isAnonymous={}", finalTargetUserId, isAnonymous);

        return toDTO(saved);
    }

    @Override
    public List<QuestionResponse> getPublicWallQuestions(String targetUserId) {
        return questionRepo.findByTargetUserIdAndStatusAndIsPublicTrueOrderByAnsweredAtDesc(targetUserId, "ANSWERED")
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionResponse> getOwnerInbox(String ownerUserId, String statusFilter) {
        List<Question> list;
        if (statusFilter != null && !statusFilter.isBlank() && !statusFilter.equalsIgnoreCase("ALL")) {
            list = questionRepo.findByTargetUserIdAndStatusOrderByCreatedAtDesc(ownerUserId, statusFilter.trim().toUpperCase());
        } else {
            list = questionRepo.findByTargetUserIdOrderByCreatedAtDesc(ownerUserId);
        }

        return list.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse replyQuestion(String ownerUserId, String questionId, ReplyQuestionRequest request) {
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        boolean isCommunity = question.getTargetUserId() == null
                || question.getTargetUserId().isBlank()
                || "community".equalsIgnoreCase(question.getTargetUserId());

        if (!isCommunity && !question.getTargetUserId().equals(ownerUserId)) {
            throw new AccessDeniedException("You are not authorized to reply to this question.");
        }

        if (ownerUserId != null && reputationService.isUserBanned(ownerUserId)) {
            throw new AccessDeniedException("Your account is currently suspended due to negative reputation points. Contact admin to appeal.");
        }

        if (aiService.unSafe(request.getAnswer())) {
            if (ownerUserId != null) {
                reputationService.penalizeHateViolation(ownerUserId, "Hateful Q&A reply submission", 25);
            }
            throw new UnsafeContentException("Reply flagged by AI moderation: violates community safety guidelines. 25 points deducted from reputation score.");
        }

        // If it was a community question, track who answered it
        if (isCommunity) {
            question.setTargetUserId(ownerUserId);
        }

        question.setAnswer(request.getAnswer().trim());
        question.setStatus("ANSWERED");
        question.setPublic(request.isPublic());
        question.setAnsweredAt(new Date());

        Question saved = questionRepo.save(question);
        if (saved == null) {
            saved = question;
        }

        if (saved.getAskerUserId() != null && !saved.getAskerUserId().isBlank()) {
            notificationService.notifyQuestionAnswered(
                    saved.getAskerUserId(),
                    saved.getId(),
                    saved.getAnswer(),
                    saved.isPublic()
            );
        }

        logger.info("Owner {} replied to questionId {}, isPublic={}", ownerUserId, questionId, request.isPublic());
        return toDTO(saved);
    }

    @Override
    public void deleteQuestion(String userId, String questionId, boolean isAdmin) {
        Question question = questionRepo.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        boolean isTarget = question.getTargetUserId() != null && question.getTargetUserId().equals(userId);
        boolean isAsker = question.getAskerUserId() != null && question.getAskerUserId().equals(userId);

        if (!isAdmin && !isTarget && !isAsker) {
            throw new AccessDeniedException("You are not authorized to delete this question.");
        }

        questionRepo.deleteById(questionId);
        logger.info("Question {} deleted by user {}", questionId, userId);
    }

    @Override
    public List<QuestionResponse> getMyAskedQuestions(String askerUserId) {
        return questionRepo.findByAskerUserIdOrderByCreatedAtDesc(askerUserId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long getPendingCount(String ownerUserId) {
        return questionRepo.countByTargetUserIdAndStatus(ownerUserId, "PENDING");
    }

    @Override
    public List<QuestionResponse> getFeedQuestions() {
        return questionRepo.findAllPublicQuestions(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private QuestionResponse toDTO(Question q) {
        String targetAlias;
        if (q.getTargetUserId() == null || q.getTargetUserId().isBlank() || "community".equalsIgnoreCase(q.getTargetUserId())) {
            targetAlias = "Community";
        } else {
            targetAlias = AnonNameUtil.generate(q.getTargetUserId());
        }

        return QuestionResponse.builder()
                .id(q.getId())
                .targetUserId(q.getTargetUserId())
                .targetAlias(targetAlias)
                .askerAlias(q.getAskerAlias())
                .isAnonymous(q.isAnonymous())
                .content(q.getContent())
                .status(q.getStatus())
                .answer(q.getAnswer())
                .isPublic(q.isPublic())
                .createdAt(q.getCreatedAt())
                .answeredAt(q.getAnsweredAt())
                .build();
    }
}

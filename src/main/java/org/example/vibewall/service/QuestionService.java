package org.example.vibewall.service;

import org.example.vibewall.DTO.QuestionRequest;
import org.example.vibewall.DTO.QuestionResponse;
import org.example.vibewall.DTO.ReplyQuestionRequest;
import org.example.vibewall.security.CustomUserDetails;

import java.util.List;

public interface QuestionService {

    QuestionResponse askQuestion(String targetUserId, QuestionRequest request, CustomUserDetails currentUser);

    List<QuestionResponse> getPublicWallQuestions(String targetUserId);

    List<QuestionResponse> getOwnerInbox(String ownerUserId, String statusFilter);

    QuestionResponse replyQuestion(String ownerUserId, String questionId, ReplyQuestionRequest request);

    void deleteQuestion(String userId, String questionId, boolean isAdmin);

    List<QuestionResponse> getMyAskedQuestions(String askerUserId);

    long getPendingCount(String ownerUserId);
}

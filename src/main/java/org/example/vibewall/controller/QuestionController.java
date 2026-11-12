package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.QuestionRequest;
import org.example.vibewall.DTO.QuestionResponse;
import org.example.vibewall.DTO.ReplyQuestionRequest;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
@Tag(name = "Anonymous Q&A", description = "Ask questions anonymously or with identity, and reply publicly or privately")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/ask/{targetUserId}")
    @Operation(summary = "Ask a question to a wall owner (anonymously or with identity)")
    public ResponseEntity<QuestionResponse> askQuestion(
            @PathVariable String targetUserId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        QuestionResponse response = questionService.askQuestion(targetUserId, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/wall/{targetUserId}")
    @Operation(summary = "Get all publicly answered Q&As on a user's wall")
    public ResponseEntity<List<QuestionResponse>> getPublicWallQuestions(
            @PathVariable String targetUserId
    ) {
        return ResponseEntity.ok(questionService.getPublicWallQuestions(targetUserId));
    }

    @GetMapping("/inbox")
    @Operation(summary = "Get current user's Q&A inbox (pending and answered)")
    public ResponseEntity<List<QuestionResponse>> getOwnerInbox(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) String status
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(questionService.getOwnerInbox(currentUser.getId(), status));
    }

    @PostMapping("/{questionId}/reply")
    @Operation(summary = "Reply to an incoming question (publicly on wall or privately)")
    public ResponseEntity<QuestionResponse> replyQuestion(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String questionId,
            @Valid @RequestBody ReplyQuestionRequest request
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(questionService.replyQuestion(currentUser.getId(), questionId, request));
    }

    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete a question")
    public ResponseEntity<Map<String, String>> deleteQuestion(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable String questionId
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        questionService.deleteQuestion(currentUser.getId(), questionId, isAdmin);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    @GetMapping("/my-asked")
    @Operation(summary = "Get questions asked by the current user to others")
    public ResponseEntity<List<QuestionResponse>> getMyAskedQuestions(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(questionService.getMyAskedQuestions(currentUser.getId()));
    }

    @GetMapping("/pending-count")
    @Operation(summary = "Get count of pending unanswered questions in owner's inbox")
    public ResponseEntity<Map<String, Long>> getPendingCount(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        long count = questionService.getPendingCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }
}

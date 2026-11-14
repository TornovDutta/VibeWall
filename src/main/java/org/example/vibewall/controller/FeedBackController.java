package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.vibewall.DTO.FeedbackRequest;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PlatformMisuseException;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Give and manage feedback on confessions")
public class FeedBackController {
    private final FeedbackService service;

    @PostMapping("/{confessionId}")
    @Operation(summary = "Add feedback to a confession")
    @ApiResponse(responseCode = "201", description = "Feedback added")
    @ApiResponse(responseCode = "404", description = "Confession not found")
    @ApiResponse(responseCode = "422", description = "Content failed safety check")
    public ResponseEntity<?> create(
            @PathVariable String confessionId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.example.vibewall.security.CustomUserDetails details,
            @Valid @RequestBody FeedbackRequest requested) throws PlatformMisuseException, ConfessionNotFoundException {
        String userId = details != null ? details.getId() : null;
        return new ResponseEntity<>(service.giveFeedback(userId, confessionId, requested), HttpStatus.CREATED);
    }

    @PostMapping("/{confessionId}/{feedbackId}/like")
    @Operation(summary = "Like a feedback comment and award points to author")
    public ResponseEntity<?> like(
            @PathVariable String confessionId,
            @PathVariable int feedbackId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.example.vibewall.security.CustomUserDetails details) throws ConfessionNotFoundException {
        String userId = details != null ? details.getId() : null;
        return ResponseEntity.ok(service.likeFeedback(confessionId, feedbackId, userId));
    }

    @PutMapping("/{confessionId}/{feedbackId}")
    @Operation(summary = "Update feedback on a confession")
    @ApiResponse(responseCode = "200", description = "Feedback updated")
    @ApiResponse(responseCode = "404", description = "Confession or feedback not found")
    public ResponseEntity<?> update(
            @PathVariable String confessionId,
            @PathVariable int feedbackId,
            @Valid @RequestBody FeedbackRequest requested) throws PlatformMisuseException, ConfessionNotFoundException {
        return ResponseEntity.ok(service.updateFeedback(confessionId, feedbackId, requested));
    }

    @DeleteMapping("/{confessionId}/{feedbackId}")
    @Operation(summary = "Delete feedback from a confession")
    @ApiResponse(responseCode = "200", description = "Feedback deleted")
    @ApiResponse(responseCode = "404", description = "Confession or feedback not found")
    public ResponseEntity<?> delete(
            @PathVariable String confessionId,
            @PathVariable int feedbackId) throws ConfessionNotFoundException {
        return ResponseEntity.ok(service.deleteFeedback(confessionId, feedbackId));
    }
}

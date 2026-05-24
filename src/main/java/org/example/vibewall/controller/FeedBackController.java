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
            @Valid @RequestBody FeedbackRequest requested) throws PlatformMisuseException, ConfessionNotFoundException {
        return new ResponseEntity<>(service.giveFeedback(confessionId, requested), HttpStatus.CREATED);
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

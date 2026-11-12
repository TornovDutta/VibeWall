package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.CreatePollRequest;
import org.example.vibewall.DTO.PollResponse;
import org.example.vibewall.DTO.VoteRequest;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.PollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
@Tag(name = "Polls", description = "Public anonymous opinion polls and live votes")
public class PollController {

    private final PollService pollService;

    @GetMapping
    @Operation(summary = "Get all active polls with live voting statistics")
    public ResponseEntity<List<PollResponse>> getPolls(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        String userId = details != null ? details.getId() : null;
        return ResponseEntity.ok(pollService.getFeedPolls(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single poll by id")
    public ResponseEntity<PollResponse> getPoll(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        String userId = details != null ? details.getId() : null;
        return ResponseEntity.ok(pollService.getPollById(id, userId));
    }

    @PostMapping
    @Operation(summary = "Create a new opinion poll (between 2 and 6 options)")
    public ResponseEntity<PollResponse> createPoll(
            @AuthenticationPrincipal CustomUserDetails details,
            @Valid @RequestBody CreatePollRequest request
    ) {
        return new ResponseEntity<>(pollService.createPoll(details.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Cast or update your vote on a poll option")
    public ResponseEntity<PollResponse> vote(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails details,
            @Valid @RequestBody VoteRequest request
    ) {
        return ResponseEntity.ok(pollService.vote(id, request.getOptionId(), details.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a poll (author or admin)")
    public ResponseEntity<Map<String, String>> deletePoll(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        boolean isAdmin = details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        pollService.deletePoll(id, details.getId(), isAdmin);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}

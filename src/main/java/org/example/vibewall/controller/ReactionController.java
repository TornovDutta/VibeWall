package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/feed/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "Anonymous supportive reactions (hugs, solidarity, relatability)")
public class ReactionController {

    private final ReactionService reactionService;

    @GetMapping("/{confessionId}")
    @Operation(summary = "Get reaction counts for a confession")
    public ResponseEntity<Map<String, Integer>> getReactions(@PathVariable String confessionId) {
        return ResponseEntity.ok(reactionService.getReactions(confessionId));
    }

    @PostMapping("/{confessionId}/{type}")
    @Operation(summary = "Add an anonymous reaction to a confession (hug, heart, relate, strength)")
    public ResponseEntity<Map<String, Integer>> addReaction(
            @PathVariable String confessionId,
            @PathVariable String type
    ) {
        return ResponseEntity.ok(reactionService.addReaction(confessionId, type));
    }
}

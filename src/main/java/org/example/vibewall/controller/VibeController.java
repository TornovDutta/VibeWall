package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.DailyVibeResponse;
import org.example.vibewall.DTO.VibeCheckRequest;
import org.example.vibewall.DTO.VibeCheckResponse;
import org.example.vibewall.service.VibeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vibes")
@RequiredArgsConstructor
@Tag(name = "Vibes", description = "Daily affirmations, quotes, and AI vibe checks")
public class VibeController {

    private final VibeService vibeService;

    @GetMapping("/daily")
    @Operation(summary = "Get the daily inspiration / affirmation quote")
    @ApiResponse(responseCode = "200", description = "Daily quote returned")
    public ResponseEntity<DailyVibeResponse> getDailyVibe() {
        return ResponseEntity.ok(vibeService.getDailyVibe());
    }

    @GetMapping("/random")
    @Operation(summary = "Get a random inspirational affirmation")
    @ApiResponse(responseCode = "200", description = "Random quote returned")
    public ResponseEntity<DailyVibeResponse> getRandomVibe() {
        return ResponseEntity.ok(vibeService.getRandomVibe());
    }

    @PostMapping("/check")
    @Operation(summary = "Analyze the emotional vibe and sentiment of a text")
    @ApiResponse(responseCode = "200", description = "Vibe analysis result")
    public ResponseEntity<VibeCheckResponse> checkVibe(@Valid @RequestBody VibeCheckRequest request) {
        return ResponseEntity.ok(vibeService.checkVibe(request));
    }
}

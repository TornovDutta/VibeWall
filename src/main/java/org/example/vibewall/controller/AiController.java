package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.EnhanceRequest;
import org.example.vibewall.DTO.EnhanceResponse;
import org.example.vibewall.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assist", description = "AI text enhancement, grammar fix, and suggestions")
public class AiController {

    private final AiService aiService;

    @PostMapping("/enhance")
    @Operation(summary = "Polish sentence and get AI suggestions for confessions, polls, or Q&A")
    @ApiResponse(responseCode = "200", description = "Polished text and suggestions generated")
    public ResponseEntity<EnhanceResponse> enhance(@Valid @RequestBody EnhanceRequest request) {
        return ResponseEntity.ok(aiService.enhanceText(request));
    }
}

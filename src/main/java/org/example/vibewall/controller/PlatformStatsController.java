package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.PlatformStatsResponse;
import org.example.vibewall.service.PlatformStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Tag(name = "Platform Statistics", description = "Live community and platform metrics")
public class PlatformStatsController {

    private final PlatformStatsService statsService;

    @GetMapping("/summary")
    @Operation(summary = "Get platform metrics and community statistics")
    @ApiResponse(responseCode = "200", description = "Live stats returned")
    public ResponseEntity<PlatformStatsResponse> getSummary() {
        return ResponseEntity.ok(statsService.getStats());
    }
}

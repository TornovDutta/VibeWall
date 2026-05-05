package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.service.FeedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feed")
@Tag(name = "Feed", description = "Public feed of anonymous confessions")
public class FeedController {
    private final FeedService service;

    @GetMapping("/")
    @Operation(summary = "Get all confessions (public, cached)")
    @ApiResponse(responseCode = "200", description = "List of confessions returned")
    public ResponseEntity<List<ConfessionResponse>> getAll() {
        return new ResponseEntity<>(service.get(), HttpStatus.OK);
    }
}

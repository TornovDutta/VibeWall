package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.vibewall.DTO.ConfessionRequest;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/confession")
@RequiredArgsConstructor
@Tag(name = "Confessions", description = "Create, update, and delete your confessions")
public class ConfessionController {
    private final ConfessionService service;

    @PostMapping
    @Operation(summary = "Post a new confession")
    @ApiResponse(responseCode = "201", description = "Confession created")
    @ApiResponse(responseCode = "422", description = "Content failed safety check")
    public ResponseEntity<?> create(
            @AuthenticationPrincipal CustomUserDetails details,
            @Valid @RequestBody ConfessionRequest requested) {
        return new ResponseEntity<>(service.create(details.getId(), requested), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update your confession")
    @ApiResponse(responseCode = "202", description = "Confession updated")
    @ApiResponse(responseCode = "403", description = "Not your confession")
    @ApiResponse(responseCode = "404", description = "Confession not found")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal CustomUserDetails details,
            @Valid @RequestBody ConfessionRequest requested,
            @PathVariable String id) throws ConfessionNotFoundException {
        return new ResponseEntity<>(service.update(details.getId(), requested, id), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete your confession")
    @ApiResponse(responseCode = "204", description = "Confession deleted")
    @ApiResponse(responseCode = "403", description = "Not your confession")
    @ApiResponse(responseCode = "404", description = "Confession not found")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal CustomUserDetails details,
            @PathVariable String id) throws ConfessionNotFoundException {
        service.delete(details.getId(), id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

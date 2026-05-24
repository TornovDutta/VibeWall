package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportRequest;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/report")
@Tag(name = "Reports", description = "Submit and manage content reports")
public class ReportController {
    private final ReportService service;

    @GetMapping("/")
    @Operation(summary = "List your submitted reports")
    @ApiResponse(responseCode = "200", description = "Reports returned")
    public ResponseEntity<List<ReportResponse>> getMyReports(
            @AuthenticationPrincipal CustomUserDetails details) {
        return ResponseEntity.ok(service.getByUser(details.getId()));
    }

    @PostMapping("/")
    @Operation(summary = "Submit a new report")
    @ApiResponse(responseCode = "201", description = "Report created")
    public ResponseEntity<?> createReport(
            @Valid @RequestBody ReportRequest requested,
            @AuthenticationPrincipal CustomUserDetails details) {
        return new ResponseEntity<>(service.create(details.getId(), requested), HttpStatus.CREATED);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update your report")
    @ApiResponse(responseCode = "202", description = "Report updated")
    @ApiResponse(responseCode = "403", description = "Not your report")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> updateReport(
            @Valid @RequestBody ReportRequest requested,
            @PathVariable String reportId,
            @AuthenticationPrincipal CustomUserDetails details) throws ReportNotFoundException {
        return new ResponseEntity<>(service.update(details.getId(), reportId, requested), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete your report")
    @ApiResponse(responseCode = "204", description = "Report deleted")
    @ApiResponse(responseCode = "403", description = "Not your report")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> deleteReport(
            @PathVariable String reportId,
            @AuthenticationPrincipal CustomUserDetails details) throws ReportNotFoundException {
        service.delete(details.getId(), reportId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

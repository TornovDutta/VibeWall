package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportRequested;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/report")
@Tag(name = "Reports", description = "Submit and manage content reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService service;

    @PostMapping("/")
    @Operation(summary = "Submit a new report")
    @ApiResponse(responseCode = "201", description = "Report created")
    public ResponseEntity<?> createReport(@RequestBody ReportRequested requested) {
        return new ResponseEntity<>(service.create(requested), HttpStatus.CREATED);
    }

    @PutMapping("/{reportId}")
    @Operation(summary = "Update a report")
    @ApiResponse(responseCode = "202", description = "Report updated")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> updateReport(
            @RequestBody ReportRequested requested,
            @PathVariable String reportId) throws ReportNotFoundException {
        return new ResponseEntity<>(service.update(reportId, requested), HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a report")
    @ApiResponse(responseCode = "204", description = "Report deleted")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> deleteReport(@PathVariable String reportId) throws ReportNotFoundException {
        service.delete(reportId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

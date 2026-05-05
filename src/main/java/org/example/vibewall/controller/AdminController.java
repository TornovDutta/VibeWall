package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only operations — requires ADMIN role")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final AdminService service;

    @GetMapping
    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "List of all users")
    public ResponseEntity<List<UsersResponse>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new admin account")
    @ApiResponse(responseCode = "201", description = "Admin created — returns JWT tokens")
    @ApiResponse(responseCode = "400", description = "Admin already exists")
    public ResponseEntity<TokenResponse> addAdmin(@Valid @RequestBody UsersRequested users) {
        return new ResponseEntity<>(service.addAdmin(users), HttpStatus.CREATED);
    }

    @PutMapping("/me")
    @Operation(summary = "Update your admin profile")
    @ApiResponse(responseCode = "200", description = "Admin updated")
    @ApiResponse(responseCode = "404", description = "Admin not found")
    public ResponseEntity<?> updateAdmin(
            @AuthenticationPrincipal CustomUserDetails details,
            @Valid @RequestBody UsersRequested user) throws AdminNotFoundException {
        return new ResponseEntity<>(service.update(details.getId(), user), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete your admin account")
    @ApiResponse(responseCode = "204", description = "Admin deleted")
    @ApiResponse(responseCode = "404", description = "Admin not found")
    public ResponseEntity<?> deleteAdmin(
            @AuthenticationPrincipal CustomUserDetails details) throws AdminNotFoundException {
        service.delete(details.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("report")
    @Operation(summary = "Get all reports")
    @ApiResponse(responseCode = "200", description = "List of all reports")
    public ResponseEntity<List<ReportResponse>> allReport() {
        return new ResponseEntity<>(service.getReport(), HttpStatus.OK);
    }

    @GetMapping("report/{id}")
    @Operation(summary = "Get a report by ID")
    @ApiResponse(responseCode = "200", description = "Report found")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable String id) throws ReportNotFoundException {
        return new ResponseEntity<>(service.getReportById(id), HttpStatus.OK);
    }

    @GetMapping("report/pending")
    @Operation(summary = "Get all pending reports")
    @ApiResponse(responseCode = "200", description = "List of pending reports")
    public ResponseEntity<List<ReportResponse>> allPendingReport() {
        return new ResponseEntity<>(service.getPending(), HttpStatus.OK);
    }

    @GetMapping("report/pending/{id}")
    @Operation(summary = "Get a pending report by ID")
    @ApiResponse(responseCode = "200", description = "Pending report found")
    public ResponseEntity<ReportResponse> getPendingReport(@PathVariable String id) {
        return new ResponseEntity<>(service.getPendingById(id), HttpStatus.OK);
    }

    @PatchMapping("report/Reviewed/{id}")
    @Operation(summary = "Mark a report as reviewed")
    @ApiResponse(responseCode = "200", description = "Report status updated")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<?> review(
            @PathVariable String id,
            @RequestParam String status) throws ReportNotFoundException {
        return new ResponseEntity<>(service.reslove(id, status), HttpStatus.OK);
    }
}

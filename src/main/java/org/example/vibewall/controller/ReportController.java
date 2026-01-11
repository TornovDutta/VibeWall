package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
public class ReportController {
    private final ReportService service;
    @PostMapping("/")
    public ResponseEntity<?> createReport(@RequestBody ReportRequested requested){
        return new ResponseEntity<>(service.create(requested), HttpStatus.CREATED);
    }
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@RequestBody ReportRequested requested, @PathVariable String reportId) throws ReportNotFoundException {
        return new ResponseEntity<>(service.update(reportId,requested), HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport( @PathVariable String reportId) throws ReportNotFoundException {
        service.delete(reportId);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }

}

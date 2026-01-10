package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportRequested;
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
    public ResponseEntity<?> createReport(@RequestBody ReportRequested requested, @RequestParam String confessionId){
        return new ResponseEntity<>(service.create(requested,confessionId), HttpStatus.CREATED);
    }
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@RequestBody ReportRequested requested, @PathVariable String reportId){
        return new ResponseEntity<>(service.update(reportId,requested), HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport( @PathVariable String reportId){
        service.delete(reportId);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }

}

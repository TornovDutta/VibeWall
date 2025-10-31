package org.example.vibewall.controller;

import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user/report")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> makeReport(@RequestBody Report report){
        return new ResponseEntity<>(service.create(report), HttpStatus.CREATED);
    }
    @DeleteMapping("{id}")
    public ResponseEntity<?> withdraw(@PathVariable String id) throws ReportNotFoundException {
        return new ResponseEntity<>(service.remove(id),HttpStatus.NO_CONTENT);
    }
    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable String id,@RequestBody Report report) throws ReportNotFoundException{
        return new ResponseEntity<>(service.update(id,report),HttpStatus.OK);
    }
}

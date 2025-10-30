package org.example.vibewall.controller;

import org.example.vibewall.model.Confession;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/confession")
public class ConfessionController {

    private final ConfessionService service;

    public ConfessionController(ConfessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String content, Authentication authentication) {
        try {

            String username = authentication.getName();
            service.create(content, username);
            return new ResponseEntity<>("Confession created successfully", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error creating confession: " + e.getMessage());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody Confession confession,Authentication authentication) {
        try {
            String username=authentication.getName();
            service.update(id, confession,username);
            return new ResponseEntity<>("Confession updated successfully", HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error updating confession: " + e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable String id,Authentication authentication) {
        String username=authentication.getName();
        try {
            service.delete(id,username);
            return new ResponseEntity<>("Confession deleted successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error deleting confession: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Confession>> show() {
        try {
            return new ResponseEntity<>(service.showAll(), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching confessions: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Confession> showById(@PathVariable String id) {
        try {
            return new ResponseEntity<>(service.show(id), HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching confession: " + e.getMessage());
        }
    }
}

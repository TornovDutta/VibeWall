package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionRequested;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/confession")
@RequiredArgsConstructor
public class ConfessionController {
    private final ConfessionService service;

    @PostMapping()
    public ResponseEntity<?> create(@AuthenticationPrincipal CustomUserDetails details,
                                               @RequestBody ConfessionRequested requested){
        String usersid=details.getId();
        return new ResponseEntity<>(service.create(usersid,requested), HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal CustomUserDetails details,
                                    @RequestBody ConfessionRequested requested, @PathVariable String id) throws ConfessionNotFoundException {
        String userId= details.getId();
        return new ResponseEntity<>(service.update(userId,requested,id),HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal CustomUserDetails details,
                                    @PathVariable String id) throws ConfessionNotFoundException {
        String userId= details.getId();
        service.delete(userId,id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

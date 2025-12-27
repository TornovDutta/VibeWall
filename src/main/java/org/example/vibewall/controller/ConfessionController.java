package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.PostRequested;
import org.example.vibewall.DTO.PostResponse;
import org.example.vibewall.config.CustomUserDetails;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/confession")
@RequiredArgsConstructor
public class ConfessionController {
    private final ConfessionService service;

    @PostMapping()
    public ResponseEntity<?> create(@AuthenticationPrincipal CustomUserDetails details,
                                               @RequestBody PostRequested requested){
        String usersid=details.getId();
        return new ResponseEntity<>(service.create(usersid,requested), HttpStatus.CREATED);
    }
}

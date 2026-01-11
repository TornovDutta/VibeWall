package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;

import org.example.vibewall.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final RegistrationService service;

    @PostMapping("")
    public ResponseEntity<UsersResponse> create(@Valid @RequestBody UsersRequested user){

        return new ResponseEntity<>(service.adduser(user), HttpStatus.CREATED);
    }


}
package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final RegistrationService service;



    @PostMapping("")
    public ResponseEntity<Users> create(@RequestBody Users user){
        return new ResponseEntity<>(service.adduser(user), HttpStatus.CREATED);
    }




}

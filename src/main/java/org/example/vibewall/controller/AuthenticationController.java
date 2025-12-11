package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequestedDTO;
import org.example.vibewall.DTO.UsersResponseDTO;
import org.example.vibewall.exception.UserNotFoundException;
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



    @PostMapping("/register")
    public ResponseEntity<UsersResponseDTO> create(@RequestBody UsersRequestedDTO user) throws UserNotFoundException {
        return new ResponseEntity<>(service.adduser(user), HttpStatus.CREATED);
    }





}

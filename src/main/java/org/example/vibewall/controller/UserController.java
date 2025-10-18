package org.example.vibewall.controller;

import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UsersService service;

    public UserController(UsersService service) {
        this.service = service;
    }

    @PostMapping("add")
    public ResponseEntity<String> addUser(@RequestBody Users user){
        try{
            service.add(user);
            return new ResponseEntity<>("add the user", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("addAdmin")
    public ResponseEntity<String> addAdmin(@RequestBody Users users){
        try{
            service.addAdmin(users);
            return new ResponseEntity<>("add the user", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}

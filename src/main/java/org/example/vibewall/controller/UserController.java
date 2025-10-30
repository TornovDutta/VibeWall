package org.example.vibewall.controller;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.RequestDelete;
import org.example.vibewall.model.RequestUpdate;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
public class UserController {
    private final UsersService service;

    public UserController(UsersService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody Users user) {
        return new ResponseEntity<>(service.add(user),HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestUpdate> updateUser(
            @PathVariable String id,
            @RequestBody Users user) throws UserNotFoundException {
        RequestUpdate request = service.update(id, user);
        return new ResponseEntity<>(request, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RequestDelete> deleteUser(
            @PathVariable String id) throws UserNotFoundException {
        RequestDelete request = service.delete(id);
        return new ResponseEntity<>(request, HttpStatus.ACCEPTED);
    }



}

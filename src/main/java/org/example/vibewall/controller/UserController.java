package org.example.vibewall.controller;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class UserController {
    private final UsersService service;



    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody Users user) {
        return new ResponseEntity<>(service.add(user),HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable String id, @RequestBody Users user) throws UserNotFoundException {
        return new ResponseEntity<>(service.update(id,user),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) throws UserNotFoundException{
        service.delete(id);
        return new ResponseEntity<>("delete the id : "+id,HttpStatus.NO_CONTENT);
    }



}

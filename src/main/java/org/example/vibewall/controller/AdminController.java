package org.example.vibewall.controller;

import org.example.vibewall.model.Users;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UsersService service;

    public AdminController(UsersService service) {
        this.service = service;
    }

    @PostMapping("add")
    public ResponseEntity<String> addAdmin(@RequestBody Users users){
        try{
            service.addAdmin(users);
            return new ResponseEntity<>("add the user", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping("update/{id}")
    public ResponseEntity<String> updateAdmin(@PathVariable String id,@RequestBody Users user){
        try{
            service.update(id,user);
            return new ResponseEntity<>("update",HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("delete")
    public ResponseEntity<String> deleteAdmin(@PathVariable String id){
        try{
            service.delete(id);
            return new ResponseEntity<>("delete",HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}

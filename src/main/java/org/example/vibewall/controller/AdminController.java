package org.example.vibewall.controller;

import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/admin")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Users>> getAll(){
        return new ResponseEntity<>(service.getAll(),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Users> addAdmin(@RequestBody Users users){
        return new ResponseEntity<>(service.addAdmin(users), HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable String id,@RequestBody Users user) throws AdminNotFoundException {

        return new ResponseEntity<>(service.update(id,user),HttpStatus.OK);

    }
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String id)throws AdminNotFoundException{
        service.delete(id);
        return new ResponseEntity<>("Delete",HttpStatus.NO_CONTENT);
    }
}

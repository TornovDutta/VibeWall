package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/confessions")
@RequiredArgsConstructor
public class ConfessionController {

    private final ConfessionService service;



    @PostMapping
    public ResponseEntity<Confession> create(@RequestBody Confession  confession,Authentication authentication) throws UserNotFoundException , PrincipalNotFollowException {
        String name=authentication.getName();
        return new ResponseEntity<>(service.create(confession,name),HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Confession confession,
                                    Authentication authentication) throws PrincipalNotFollowException {
        String userName=authentication.getName();
        service.update(id,confession,userName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> delete(@PathVariable String id,Authentication authentication) {
        String username=authentication.getName();
        service.delete(id,username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<Confession>> show() {
       return new ResponseEntity<>(service.showAll(),HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Confession> showById(@PathVariable String id) {
        return new ResponseEntity<>(service.show(id), HttpStatus.OK);
    }
}

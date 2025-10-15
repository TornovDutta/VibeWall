package org.example.vibewall.controller;

import org.example.vibewall.model.Confession;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/confession")
public class ConfessionController {
    private final ConfessionService service;

    public ConfessionController(ConfessionService service) {
        this.service = service;
    }

    @PostMapping("create")
    public ResponseEntity<String> create(@RequestBody Confession confession){
        try{
            service.create(confession);
            return new ResponseEntity<>("Created", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @PutMapping("update/{id}")
    public ResponseEntity<String> update(@PathVariable String id,@RequestBody Confession confession){
        try{
            service.update(id,confession);
            return new ResponseEntity<>("update",HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("delete/{id}")
    public  ResponseEntity<String> update(@PathVariable  String id){
        try{
            service.delete(id);
            return new ResponseEntity<>("delete",HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}

package org.example.vibewall.controller;

import org.example.vibewall.model.Confession;
import org.example.vibewall.service.ConfessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/confession")
public class ConfessionController {
    private final ConfessionService service;

    public ConfessionController(ConfessionService service) {
        this.service = service;
    }

    @PostMapping("create")
    public ResponseEntity<String> create(@RequestBody Confession confession){
        service.create(confession);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);

    }
}

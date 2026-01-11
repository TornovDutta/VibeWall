package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;
import org.example.vibewall.service.FeedService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/feed")
public class FeedController {
    private final FeedService service;

    @GetMapping("/")
    public ResponseEntity<List<ConfessionResponse>> getAll(){
        return new ResponseEntity<>(service.get(), HttpStatus.OK);
    }
}

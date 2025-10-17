package org.example.vibewall.controller;

import org.example.vibewall.model.Feedback;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("{id}")
    public ResponseEntity<String> create(@PathVariable String id, @RequestBody Feedback feedback){
        try{

            service.giveFeedback(id,feedback);
            return new ResponseEntity<>("create the feedback", HttpStatus.CREATED);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("{id}")
    public ResponseEntity<List<Feedback>> getAll(@PathVariable String id){
        try{
            return new ResponseEntity<>(service.get(id),HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}

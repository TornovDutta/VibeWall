package org.example.vibewall.controller;

import org.example.vibewall.model.Feedback;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("")
    public ResponseEntity<String> create(@RequestBody Feedback feedback){
        try{
            service.giveFeedback(feedback);
            return new ResponseEntity<>("create the feedback", HttpStatus.CREATED);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}

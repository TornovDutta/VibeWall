package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.FeedbackNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService service;


    @PostMapping("/confession/{confessionId}")
    public ResponseEntity<String> create(@PathVariable String confessionId,
                                         @RequestBody Feedback feedback)
            throws PrincipalNotFollowException, ConfessionNotFoundException {
        return new ResponseEntity<>(service.giveFeedback(confessionId, feedback), HttpStatus.CREATED);
    }


    @GetMapping("/confession/{confessionId}")
    public ResponseEntity<List<Feedback>> getAll(@PathVariable String confessionId)
            throws ConfessionNotFoundException {
        return new ResponseEntity<>(service.get(confessionId), HttpStatus.OK);
    }


    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> get(@PathVariable String feedbackId)
            throws FeedbackNotFoundException {
        return new ResponseEntity<>(service.getById(feedbackId), HttpStatus.OK);
    }


    @PutMapping("/{feedbackId}")
    public ResponseEntity<String> update(@PathVariable String feedbackId,
                                         @RequestBody Feedback feedback)
            throws PrincipalNotFollowException, FeedbackNotFoundException {
        return new ResponseEntity<>(service.update(feedbackId, feedback), HttpStatus.OK);
    }


    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> delete(@PathVariable String feedbackId)
            throws FeedbackNotFoundException {
        service.delete(feedbackId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

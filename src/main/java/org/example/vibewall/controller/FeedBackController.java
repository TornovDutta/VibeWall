package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.FeedbackRequested;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/feedback")
@RequiredArgsConstructor
public class FeedBackController {
    private final FeedbackService service;
    @PostMapping("/{confessionId}")
    public ResponseEntity<?> create(@PathVariable String confessionId, @RequestBody FeedbackRequested requested) throws PrincipalNotFollowException, ConfessionNotFoundException {
       return new ResponseEntity<>(service.giveFeedback(confessionId,requested), HttpStatus.CREATED);

    }
    @PutMapping("/{confessionId}/{feedbackId}")
    public ResponseEntity<?> update(
            @PathVariable String confessionId,
            @PathVariable int feedbackId,
            @RequestBody FeedbackRequested requested)
            throws PrincipalNotFollowException, ConfessionNotFoundException {

        return ResponseEntity.ok(
                service.updateFeedback(confessionId, feedbackId, requested)
        );
    }
    @DeleteMapping("/{confessionId}/{feedbackId}")
    public ResponseEntity<?> delete(
            @PathVariable String confessionId,
            @PathVariable int feedbackId)
            throws ConfessionNotFoundException {

        return ResponseEntity.ok(
                service.deleteFeedback(confessionId, feedbackId)
        );
    }

}

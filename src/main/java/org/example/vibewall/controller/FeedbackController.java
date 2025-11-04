package org.example.vibewall.controller;

import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.model.Feedback;
import org.example.vibewall.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping("{id}")
    public ResponseEntity<String> create(@PathVariable String id, @RequestBody Feedback feedback) throws PrincipalNotFollowException {
        try{

            service.giveFeedback(id,feedback);
            return new ResponseEntity<>("create the feedback", HttpStatus.CREATED);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("confession/{id}")
    public ResponseEntity<List<Feedback>> getAll(@PathVariable String id){
        try{
            return new ResponseEntity<>(service.get(id),HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("{id}")
    public ResponseEntity<Feedback> get(@PathVariable String id){
        try{
            return new ResponseEntity<>(service.getById(id),HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping("update/{id}")
    public ResponseEntity<String> update(@PathVariable String id,@RequestBody Feedback feedback) throws PrincipalNotFollowException{
        try{
            return new ResponseEntity<>(service.update(id,feedback),HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable String id){
        try{
            service.delete(id);
            return new ResponseEntity<>("Delete",HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.model.Report;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;


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
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("report")
    public ResponseEntity<List<Report>> allReport(){
        return new ResponseEntity<>(service.getReport(),HttpStatus.OK);
    }
    @GetMapping("report/{id}")
    public ResponseEntity<Report> allReport(@PathVariable String id) throws ReportNotFoundException {
        return new ResponseEntity<>(service.getReportById(id),HttpStatus.OK);
    }
    @GetMapping("report/pending")
    public ResponseEntity<List<Report>> allPendingReport(){
        return new ResponseEntity<>(service.getPending(),HttpStatus.OK);
    }
    @GetMapping("report/pending/{id}")
    public ResponseEntity<Report> allPendingReport(@PathVariable String id){
        return new ResponseEntity<>(service.getPendingById(id),HttpStatus.OK);
    }
    @PatchMapping("report/Reviewed/{id}/{status}")
    public ResponseEntity<?> review(@PathVariable String id,@PathVariable String status) throws ReportNotFoundException{
        return new ResponseEntity<>(service.reslove(id,status),HttpStatus.OK);
    }

}

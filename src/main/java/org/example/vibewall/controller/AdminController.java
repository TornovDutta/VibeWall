package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportDTO;
import org.example.vibewall.DTO.UsersDTO;
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
    public ResponseEntity<List<UsersDTO>> getAll(){
        return new ResponseEntity<>(service.getAll(),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UsersDTO> addAdmin(@RequestBody Users users){
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
    @GetMapping("reports")
    public ResponseEntity<List<ReportDTO>> allReport(){
        return new ResponseEntity<>(service.getReport(),HttpStatus.OK);
    }
    @GetMapping("reports/{id}")
    public ResponseEntity<ReportDTO> allReport(@PathVariable String id) throws ReportNotFoundException {
        return new ResponseEntity<>(service.getReportById(id),HttpStatus.OK);
    }
    @GetMapping("reports/pending")
    public ResponseEntity<List<ReportDTO>> allPendingReport(){
        return new ResponseEntity<>(service.getPending(),HttpStatus.OK);
    }
    @GetMapping("reports/pending/{id}")
    public ResponseEntity<Report> allPendingReport(@PathVariable String id){
        return new ResponseEntity<>(service.getPendingById(id),HttpStatus.OK);
    }
    @PatchMapping("reports/Reviewed/{id}/{status}")
    public ResponseEntity<?> review(@PathVariable String id,@PathVariable String status) throws ReportNotFoundException{
        return new ResponseEntity<>(service.reslove(id,status),HttpStatus.OK);
    }

}

package org.example.vibewall.controller;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ReportResponse;
import org.example.vibewall.DTO.TokenResponse;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.security.CustomUserDetails;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.exception.ReportNotFoundException;
import org.example.vibewall.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor

public class AdminController {
    private final AdminService service;


    @GetMapping
    public ResponseEntity<List<UsersResponse>> getAll(){
        return new ResponseEntity(service.getAll(),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TokenResponse> addAdmin(@Valid  @RequestBody UsersRequested users){
        return new ResponseEntity<>(service.addAdmin(users), HttpStatus.CREATED);
    }
    @PutMapping("/me")
    public ResponseEntity<?> updateAdmin(@AuthenticationPrincipal CustomUserDetails details,
                                         @Valid @RequestBody UsersRequested user) throws AdminNotFoundException {
        String id=details.getId();
        return new ResponseEntity<>(service.update(id,user),HttpStatus.OK);

    }
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAdmin(@AuthenticationPrincipal CustomUserDetails details)throws AdminNotFoundException{
        String id=details.getId();
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("report")
    public ResponseEntity<List<ReportResponse>> allReport(){
        return new ResponseEntity<>(service.getReport(),HttpStatus.OK);
    }
    @GetMapping("report/{id}")
    public ResponseEntity<ReportResponse> allReport(@PathVariable String id) throws ReportNotFoundException {
        return new ResponseEntity<>(service.getReportById(id),HttpStatus.OK);
    }
    @GetMapping("report/pending")
    public ResponseEntity<List<ReportResponse>> allPendingReport(){
        return new ResponseEntity<>(service.getPending(),HttpStatus.OK);
    }
    @GetMapping("report/pending/{id}")
    public ResponseEntity<ReportResponse> allPendingReport(@PathVariable String id){
        return new ResponseEntity<>(service.getPendingById(id),HttpStatus.OK);
    }
    @PatchMapping("report/Reviewed/{id}")
    public ResponseEntity<?> review(@PathVariable String id,@RequestParam String status) throws ReportNotFoundException{
        return new ResponseEntity<>(service.reslove(id,status),HttpStatus.OK);
    }

}
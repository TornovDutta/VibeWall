package org.example.vibewall.controller;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersRequested;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.config.CustomUserDetails;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.example.vibewall.service.CustomUserDetailsService;
import org.example.vibewall.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UsersService service;



    @PutMapping("/me")
    public ResponseEntity<UsersResponse> updateUser(@RequestBody UsersRequested user,
                                                    @AuthenticationPrincipal CustomUserDetails details)

            throws UserNotFoundException {
        String id=details.getId();
        return new ResponseEntity<>(service.update(id, user), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails details)
            throws UserNotFoundException {
        String id= details.getId();
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}

package org.example.vibewall.service;

import org.example.vibewall.DAO.RequestDeleteRepo;
import org.example.vibewall.DAO.RequestUpdateRepo;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.RequestDelete;
import org.example.vibewall.model.RequestUpdate;
import org.example.vibewall.model.Users;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
public class UsersService {

    private final UsersRepo repo;
    private final RequestUpdateRepo requestUpdateRepo;
    private final PasswordEncoder passwordEncoder;
    private final RequestDeleteRepo requestDelete;

    public UsersService(UsersRepo repo, PasswordEncoder passwordEncoder, RequestUpdateRepo requestUpdateRepo,
                        RequestDeleteRepo requestDelete) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.requestUpdateRepo = requestUpdateRepo;
        this.requestDelete=requestDelete;
    }

    public String add(Users user) {
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user).getId();
    }



    public RequestUpdate update(String id, Users user) throws UserNotFoundException{

        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));


        RequestUpdate requestUpdate = new RequestUpdate();


        requestUpdate.setUserId(existingUser.getId());
        requestUpdate.setOldUsername(existingUser.getUsername());
        requestUpdate.setNewUsername(user.getUsername());
        requestUpdate.setOldPassword(existingUser.getPassword());
        requestUpdate.setNewPassword(passwordEncoder.encode(user.getPassword()));
        requestUpdate.setStatus("PENDING");


        return requestUpdateRepo.save(requestUpdate);
    }




    public RequestDelete delete(String id) throws UserNotFoundException{
        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " not found"));


        RequestDelete request = new RequestDelete();
        request.setUserId(existingUser.getId());
        request.setRequestType("DELETE_ACCOUNT");
        request.setStatus("PENDING");
        request.setUsername(existingUser.getUsername());
        request.setCreatedAt(LocalDateTime.now());

        return requestDelete.save(request);
    }



}

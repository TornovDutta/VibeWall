package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Users;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsersService {

    private final UsersRepo repo;

    private final PasswordEncoder passwordEncoder;


    public UsersService(UsersRepo repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;

    }

    public String add(Users user) {
        user.setUsername(passwordEncoder.encode(user.getUsername()));
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user).getId();
    }



    public Users update(String id, Users user) {

        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        existingUser.setUsername(passwordEncoder.encode(user.getUsername()));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(existingUser);

    }


    public void delete(String id) throws UserNotFoundException {
       repo.findById(id).orElseThrow(()->
               new UserNotFoundException("user of id: "+id+"not found"));
       repo.removeById(id);
    }



}

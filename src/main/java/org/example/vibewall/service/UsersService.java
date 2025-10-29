package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.model.Users;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UsersService {

    private final UsersRepo repo;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepo repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public Users add(Users user) {
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }



    public Users update(String id, Users user){
        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("admin not found with id: " + id));

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));

        return repo.save(existingUser);
    }



    public void delete(String id) {
        repo.deleteById(id);
    }

    public List<Users> getAll() {
        return repo.findAll();
    }
}

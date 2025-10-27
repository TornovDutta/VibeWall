package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.model.Users;
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

    public void add(Users user) {
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
    }

    public void addAdmin(Users user) {
        user.setRole("ADMIN");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
    }

    public void update(String id,Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repo.save(user);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}

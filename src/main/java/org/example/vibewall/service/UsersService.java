package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Service;

@Service
public class UsersService {
    private final UsersRepo repo;

    public UsersService(UsersRepo repo) {
        this.repo = repo;
    }

    public void add(Users user) {
        user.setRole("USER");
        repo.save(user);
    }

    public void addAdmin(Users user) {
        user.setRole("ADMIN");
        repo.save(user);
    }
}

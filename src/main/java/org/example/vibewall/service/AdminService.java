package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.exception.AdminNotFoundException;
import org.example.vibewall.model.Users;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final UsersRepo repo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UsersRepo repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Users> getAll() {
        return repo.findAll();
    }

    public Users addAdmin(Users user) {
        return repo.save(user);
    }

    public Users update(String id, Users user) throws AdminNotFoundException{
        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));

        return repo.save(existingUser);
    }

    public void delete(String id) throws AdminNotFoundException{
        Users existingUser = repo.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("admin not found with id: " + id));

        repo.removeById(id);
    }
}

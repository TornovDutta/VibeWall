package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UsersRepo repo;

    public RegistrationService(UsersRepo repo) {
        this.repo = repo;
    }
    public Users adduser(Users user){
        return repo.save(user);

    }
}

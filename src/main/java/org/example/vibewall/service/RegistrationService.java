package org.example.vibewall.service;

import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UsersRepo repo;
    private final static Logger logger= LoggerFactory.getLogger(RegistrationService.class);

    public RegistrationService(UsersRepo repo) {
        this.repo = repo;
    }
    public Users adduser(Users user){
        logger.info(user+" add");
        return repo.save(user);

    }
}

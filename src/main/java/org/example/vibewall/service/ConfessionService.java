package org.example.vibewall.service;

import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConfessionService {
    private final ConfessionRepo repo;
    private final UsersRepo usersRepo;

    public ConfessionService(ConfessionRepo repo, UsersRepo usersRepo) {
        this.repo = repo;
        this.usersRepo = usersRepo;
    }

    public void create(Confession confession, String userName) {
        Users user = usersRepo.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));
        confession.setUserId(user.getId());
        repo.save(confession);
    }

    public void delete(String id) {
        repo.removeById(id);
    }

    public void update(String id, Confession confession) {
        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with id: " + id));

        existingConfession.setContent(confession.getContent());
        existingConfession.setTime(new Date());

        repo.save(existingConfession);
    }


    public List<Confession> showAll() {
        return repo.findAll();
    }

    public Confession show(String id) {
        return repo.findById(id).orElseThrow();
    }
}

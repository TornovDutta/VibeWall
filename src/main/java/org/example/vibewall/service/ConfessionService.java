package org.example.vibewall.service;

import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.model.Confession;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ConfessionService {
    private final ConfessionRepo repo;

    public ConfessionService(ConfessionRepo repo) {
        this.repo = repo;
    }

    public void create(Confession confession) {
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

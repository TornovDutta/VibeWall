package org.example.vibewall.service;

import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.model.Confession;
import org.springframework.stereotype.Service;

@Service
public class ConfessionService {
    private final ConfessionRepo repo;

    public ConfessionService(ConfessionRepo repo) {
        this.repo = repo;
    }

    public void create(Confession confession) {
        repo.save(confession);
    }
}

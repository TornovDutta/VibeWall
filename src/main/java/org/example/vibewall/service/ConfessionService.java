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

    public void create(String content, String userName) {
        Users user = usersRepo.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found: " + userName));
        String id=user.getId();
        Confession confession=new Confession();
        confession.setContent(content);
        confession.setUserId(id);
        repo.save(confession);
    }

    public void delete(String id, String username) {

        Optional<Users> userOpt = usersRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        Users user = userOpt.get();


        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with id: " + id));


        boolean isOwner = existingConfession.getUserId().equals(user.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized to delete this confession");
        }


        repo.removeById(id);
    }


    public void update(String id, Confession confession, String username) {

        Optional<Users> userOpt = usersRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        Users user = userOpt.get();


        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with id: " + id));


        boolean isOwner = existingConfession.getUserId().equals(user.getId());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized to update this confession");
        }


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

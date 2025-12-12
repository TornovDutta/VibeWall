package org.example.vibewall.service;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ConfessionService {
    private final ConfessionRepo repo;
    private final UsersRepo usersRepo;
    private final Encryption encryption;
    private final OpenAiService openAiService;


    public Confession create(Confession confession, String username)
            throws UserNotFoundException, PrincipalNotFollowException {
        if (openAiService.safe(confession.getContent())) {
            throw new PrincipalNotFollowException("Confession violates safety policy");
        }
        Users user = usersRepo.findByUsername(encryption.encode(username))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        confession.setContent(encryption.encode(confession.getContent()));


        return repo.save(confession);
    }


    public void delete(String id, String username) {
        Users user = usersRepo.findByUsername(encryption.encode(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with ID: " + id));



        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());


        repo.removeById(id);
    }

    public void update(String id, Confession updatedConfession, String username)
            throws PrincipalNotFollowException {
        if (openAiService.safe(updatedConfession.getContent())) {
            throw new PrincipalNotFollowException("Confession violates safety policy");
        }
        Users user = usersRepo.findByUsername(encryption.encode(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with ID: " + id));



        existingConfession.setContent(encryption.encode(updatedConfession.getContent()));


        repo.save(existingConfession);
    }

    public List<Confession> showAll() {
        List<Confession> confessions = repo.findAll();
        confessions.forEach(confession ->
                confession.setContent(encryption.decode(confession.getContent()))
        );
        return confessions;
    }

    public Confession show(String id) {
        Confession confession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with ID: " + id));

        confession.setContent(encryption.decode(confession.getContent()));
        return confession;
    }
}

package org.example.vibewall.service;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DAO.ConfessionRepo;
import org.example.vibewall.DAO.UsersRepo;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.PrincipalNotFollowException;
import org.example.vibewall.exception.UserNotFoundException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Users;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfessionService {
    private final ConfessionRepo repo;
    private final UsersRepo usersRepo;
    private final Encryption encryption;
    private final OpenAiService openAiService;



    public Confession create(Confession confession, String userName) throws UserNotFoundException,PrincipalNotFollowException {
        if(openAiService.safe(confession.getContent())){
            throw new PrincipalNotFollowException();
        }
        Optional<Users> user = usersRepo.findByUsername(encryption.encode(userName));
        String id=user.get().getId();

        confession.setContent(encryption.encode(confession.getContent()));
        confession.setUserId(id);
        return repo.save(confession);
    }

    public void delete(String id,String username) {


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


    public void update(String id, Confession confession,String username) throws PrincipalNotFollowException {
        if(openAiService.safe(confession.getContent())){
            throw new PrincipalNotFollowException();
        }

        Optional<Users> userOpt = usersRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        Users user = userOpt.get();


        Confession existingConfession = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Confession not found with id: " + id));


        if(user.getId()!=id){
            throw new RuntimeException("you are not the owner of this confession");
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

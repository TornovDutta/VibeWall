package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionRequested;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.UnSafeExecption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.ConfessionService;
import org.example.vibewall.service.AiService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfessionServiceImplement implements ConfessionService {
    private final UsersRepo usersRepo;
    private final ConfessionRepo confessionRepo;
    private final ConfessionMapper mapper;
    private final Encryption encryption;
    private final AiService aiService;

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse create(String userId, ConfessionRequested requested) {

        if(aiService.unSafe(requested.getContent())){
            throw new UnSafeExecption("unsafe");
        }



        Confession confession = new Confession(encryption.encode(requested.getContent()),userId);

        confessionRepo.save(confession);


        return mapper.toDTO(confession);
    }

    @Override

    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse update(String userId, ConfessionRequested requested, String id) throws ConfessionNotFoundException {
        Confession confession=confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if(!confession.getUserId().equals(userId)){
            throw new AccessDeniedException("access denied");
        }
        if(aiService.unSafe(requested.getContent())){
            throw new UnSafeExecption("unsafe");
        }
        Confession newConfession = new Confession(encryption.encode(requested.getContent()),userId);

        confessionRepo.save(newConfession);
        return mapper.toDTO(newConfession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public void delete(String userId, String id) throws ConfessionNotFoundException {
        Confession confession=confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if(!confession.getUserId().equals(userId)){
            throw new AccessDeniedException("access denied");
        }
        confessionRepo.removeById(id);

    }

}

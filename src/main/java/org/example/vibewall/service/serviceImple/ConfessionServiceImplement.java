package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.PostRequested;
import org.example.vibewall.DTO.PostResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.UnSafeExecption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.repo.UsersRepo;
import org.example.vibewall.service.ConfessionService;
import org.example.vibewall.service.OpenAiService;
import org.example.vibewall.utility.PostMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfessionServiceImplement implements ConfessionService {
    private final UsersRepo usersRepo;
    private final ConfessionRepo confessionRepo;
    private final PostMapper mapper;
    private final Encryption encryption;
    private final OpenAiService openAiService;

    @Override
    public PostResponse create(String userId, PostRequested requested) {

        if(openAiService.safe(requested.content())){
            throw new UnSafeExecption("unsafe");
        }



        Confession confession = new Confession();
        confession.setContent(encryption.encode(requested.content()));
       confession.setUserId(userId);
        confessionRepo.save(confession);


        return mapper.toDTO(confession);
    }

    @Override
    public PostResponse update(String userId, PostRequested requested, String id) throws ConfessionNotFoundException {
        Confession confession=confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if(!confession.getUserId().equals(userId)){
            throw new AccessDeniedException("access denied");
        }
        if(openAiService.safe(requested.content())){
            throw new UnSafeExecption("unsafe");
        }
        confession.setContent(encryption.encode(requested.content()));

        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }

    @Override
    public void delete(String userId, String id) throws ConfessionNotFoundException {
        Confession confession=confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if(!confession.getUserId().equals(userId)){
            throw new AccessDeniedException("access denied");
        }
        confessionRepo.removeById(id);

    }

}

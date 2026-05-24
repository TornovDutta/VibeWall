package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionRequest;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.exception.AccessDeniedException;
import org.example.vibewall.exception.ConfessionNotFoundException;
import org.example.vibewall.exception.UnsafeContentException;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.repo.UserRepository;
import org.example.vibewall.service.ConfessionService;
import org.example.vibewall.service.AiService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfessionServiceImpl implements ConfessionService {
    private final UserRepository usersRepo;
    private final ConfessionRepository confessionRepo;
    private final ConfessionMapper mapper;
    private final Encryption encryption;
    private final AiService aiService;

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse create(String userId, ConfessionRequest requested) {

        if (aiService.unSafe(requested.getContent())) {
            throw new UnsafeContentException("unsafe");
        }

        Confession confession = new Confession(encryption.encode(requested.getContent()), userId);

        confessionRepo.save(confession);

        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public ConfessionResponse update(String userId, ConfessionRequest requested, String id) throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if (!confession.getUserId().equals(userId)) {
            throw new AccessDeniedException("access denied");
        }
        if (aiService.unSafe(requested.getContent())) {
            throw new UnsafeContentException("unsafe");
        }
        confession.setContent(encryption.encode(requested.getContent()));
        confessionRepo.save(confession);
        return mapper.toDTO(confession);
    }

    @Override
    @CacheEvict(
            value = {"confession-feed"},
            allEntries = true
    )
    public void delete(String userId, String id) throws ConfessionNotFoundException {
        Confession confession = confessionRepo.findById(id).orElseThrow(()->
                new ConfessionNotFoundException("wrong id"));
        if (!confession.getUserId().equals(userId)) {
            throw new AccessDeniedException("access denied");
        }
        confessionRepo.removeById(id);
    }

}

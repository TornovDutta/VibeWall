package org.example.vibewall.service.serviceImple;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepo;
import org.example.vibewall.service.FeedService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImplement implements FeedService {
    private final ConfessionRepo repo;
    private final ConfessionMapper mapper;


    @Override

    public List<ConfessionResponse> get() {
        List<Confession> confessions = repo.findAll();
        return mapper.toDTO(confessions);
    }
}

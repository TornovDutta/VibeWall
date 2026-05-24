package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.model.Confession;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.service.FeedService;
import org.example.vibewall.utility.ConfessionMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final ConfessionRepository repo;
    private final ConfessionMapper mapper;

    @Override
    @Cacheable(value = "confession-feed")
    public List<ConfessionResponse> get() {
        List<Confession> confessions = repo.findAll();
        return mapper.toDTO(confessions);
    }
}

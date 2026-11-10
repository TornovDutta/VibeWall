package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.PlatformStatsResponse;
import org.example.vibewall.repo.ConfessionRepository;
import org.example.vibewall.repo.FeedbackRepository;
import org.example.vibewall.service.PlatformStatsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformStatsServiceImpl implements PlatformStatsService {

    private final ConfessionRepository confessionRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public PlatformStatsResponse getStats() {
        long confessionsCount = confessionRepository.count();
        long feedbacksCount = feedbackRepository.count();

        return PlatformStatsResponse.builder()
                .activeConfessions(confessionsCount)
                .totalFeedbacks(feedbacksCount)
                .communityVibe("Empathetic & Supportive")
                .safetyScore("99.4% Safe")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

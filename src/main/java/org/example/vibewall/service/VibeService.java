package org.example.vibewall.service;

import org.example.vibewall.DTO.DailyVibeResponse;
import org.example.vibewall.DTO.VibeCheckRequest;
import org.example.vibewall.DTO.VibeCheckResponse;

public interface VibeService {
    DailyVibeResponse getDailyVibe();
    DailyVibeResponse getRandomVibe();
    VibeCheckResponse checkVibe(VibeCheckRequest request);
}

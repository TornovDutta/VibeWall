package org.example.vibewall.service;

import org.example.vibewall.DTO.AiStatusResponse;
import org.example.vibewall.DTO.EnhanceRequest;
import org.example.vibewall.DTO.EnhanceResponse;

public interface AiService {
    String getResponse(String prompt);
    boolean unSafe(String str);
    AiStatusResponse getAiStatus();
    AiStatusResponse testAiConnection();
    EnhanceResponse enhanceText(EnhanceRequest request);
}

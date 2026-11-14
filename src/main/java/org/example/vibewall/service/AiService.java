package org.example.vibewall.service;

import org.example.vibewall.DTO.AiStatusResponse;

public interface AiService {
    String getResponse(String prompt);
    boolean unSafe(String str);
    AiStatusResponse getAiStatus();
    AiStatusResponse testAiConnection();
}

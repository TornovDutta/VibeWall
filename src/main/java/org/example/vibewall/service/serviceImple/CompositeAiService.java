package org.example.vibewall.service.serviceImple;

import org.example.vibewall.service.AiService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Dual-model content moderation: blocks content if Gemini OR OpenRouter flags it as unsafe.
 * Gemini runs first; OpenRouter is called only when Gemini clears the content.
 */
@Service
@Primary
public class CompositeAiService implements AiService {

    private final GeminiService geminiService;
    private final OpenRouterService openRouterService;

    public CompositeAiService(GeminiService geminiService, OpenRouterService openRouterService) {
        this.geminiService = geminiService;
        this.openRouterService = openRouterService;
    }

    @Override
    public String getResponse(String prompt) {
        return geminiService.getResponse(prompt);
    }

    @Override
    public boolean unSafe(String str) {
        // Gemini check first — short-circuit if already flagged
        if (geminiService.unSafe(str)) {
            return true;
        }
        // Second opinion from OpenRouter (Mistral) — catches what Gemini may miss
        return openRouterService.unSafe(str);
    }
}

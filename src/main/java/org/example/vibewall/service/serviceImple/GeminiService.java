package org.example.vibewall.service.serviceImple;

import jakarta.annotation.PostConstruct;
import org.example.vibewall.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService implements AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @PostConstruct
    public void checkKey() {
        System.out.println("GEMINI KEY = " + apiKey);
    }


    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=%s";

    @Override
    public String getResponse(String prompt) {

        String url = String.format(GEMINI_URL, apiKey);

        String body = """
        {
          "contents": [{
            "parts": [{
              "text": "%s"
            }]
          }]
        }
        """.formatted(prompt.replace("\"", "\\\""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        System.out.println(response.getBody());

        return extractText(response.getBody());
    }

    @Override
    public boolean unSafe(String str) {

        String prompt = """
                You are a content safety checker for the platform VibeWall.

                VibeWall is an anonymous and secure emotion-sharing platform for students. 
                It allows emotional expression but strictly prohibits harmful, offensive, or unsafe content.

                ### Platform Principles ###
                1. Content must not include hate speech, harassment, bullying, or threats.
                2. Content must not promote self-harm, violence, discrimination, or illegal activity.
                3. Content must not contain explicit, abusive, or sexual content.
                4. Emotional sharing is encouraged, but safety and positivity are priorities.

                Analyze the following user message and respond **only with a single digit**:
                - Respond **1** if the message violates any principle.
                - Respond **0** if the message fully follows all principles.

                Message: "%s"
                """.formatted(str);

        String result = getResponse(prompt).trim();
        System.out.println(result);

        if (result.equals("1")) return true;
        if (result.equals("0")) return false;

        return true; // fail-safe
    }

    // 🔽 Simple text extractor (no Jackson needed)
    private String extractText(String json) {
        // crude but works for Gemini response
        int index = json.indexOf("\"text\":");
        if (index == -1) return "";

        int start = json.indexOf("\"", index + 7) + 1;
        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }
}

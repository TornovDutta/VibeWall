package org.example.vibewall.service.serviceImple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.vibewall.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Primary
public class GeminiService implements AiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s";

    private static final String[] HARM_CATEGORIES = {
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_DANGEROUS_CONTENT",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT"
    };

    @Override
    public String getResponse(String prompt) {
        try {
            String body = buildBody(prompt, false);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    String.format(GEMINI_URL, apiKey),
                    new HttpEntity<>(body, headers),
                    String.class);

            return extractText(response.getBody());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean unSafe(String str) {
        try {
            String prompt = "You are a strict content moderator for a student platform. " +
                    "Reply with ONLY the digit 1 if the message is harmful, violent, threatening, " +
                    "abusive, promotes self-harm, hate speech, or illegal activity. " +
                    "Reply with ONLY the digit 0 if it is safe. No explanation. Just 1 or 0.\n\n" +
                    "Message: " + str;

            // Use strict safety thresholds so Gemini also applies its own filters
            String body = buildBody(prompt, true);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    String.format(GEMINI_URL, apiKey),
                    new HttpEntity<>(body, headers),
                    String.class);

            String responseBody = response.getBody();
            if (responseBody == null) return false;

            // Gemini blocked the content itself → definitely unsafe
            if (responseBody.contains("blockReason") || responseBody.contains("\"SAFETY\"")) {
                return true;
            }

            String text = extractText(responseBody).trim();

            // Accept "1" anywhere in a short reply (handles "1.", "1\n", etc.)
            if (text.length() <= 5) {
                return text.contains("1");
            }

            // Longer reply: look for explicit unsafe signals
            String lower = text.toLowerCase();
            return lower.startsWith("1") ||
                    lower.contains("unsafe") ||
                    lower.contains("violates") ||
                    lower.contains("harmful");

        } catch (Exception e) {
            return false;
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String buildBody(String prompt, boolean strictSafety) throws Exception {
        ObjectNode part = objectMapper.createObjectNode().put("text", prompt);
        ArrayNode parts = objectMapper.createArrayNode().add(part);
        ObjectNode content = objectMapper.createObjectNode().set("parts", parts);
        ArrayNode contents = objectMapper.createArrayNode().add(content);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("contents", contents);

        if (strictSafety) {
            ArrayNode safety = objectMapper.createArrayNode();
            for (String category : HARM_CATEGORIES) {
                safety.add(objectMapper.createObjectNode()
                        .put("category", category)
                        .put("threshold", "BLOCK_LOW_AND_ABOVE"));
            }
            root.set("safetySettings", safety);
        }

        return objectMapper.writeValueAsString(root);
    }

    private String extractText(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();
        } catch (Exception e) {
            return "";
        }
    }
}

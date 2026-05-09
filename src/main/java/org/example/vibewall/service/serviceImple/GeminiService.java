package org.example.vibewall.service.serviceImple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.vibewall.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
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

    private static final String MODERATION_PROMPT =
            "You are the strictest content moderator for a student social platform called VibeWall. " +
            "Your job is to BLOCK any message that breaks platform principles. " +
            "Reply with ONLY the digit 1 (BLOCK) if the message contains ANY of the following:\n" +
            "- Insults, name-calling, slurs, or disrespectful language directed at any person or group\n" +
            "- Personal attacks, bullying, put-downs, or language that demeans or humiliates someone\n" +
            "- Threats, intimidation, coercion, or blackmail of any kind\n" +
            "- Harassment or targeted mockery\n" +
            "- Hate speech based on race, religion, gender, caste, nationality, sexuality, or any identity\n" +
            "- Violence, gore, or content that glorifies harm\n" +
            "- Self-harm promotion or encouragement of suicide\n" +
            "- Sexually explicit or inappropriate content\n" +
            "- Promotion of illegal activities or substances\n" +
            "- Shaming, guilt-tripping, or emotionally manipulating others\n" +
            "- Any content that would make a person feel unsafe, unwelcome, or degraded\n\n" +
            "Reply with ONLY the digit 0 (ALLOW) ONLY if the message is respectful and safe for all users. " +
            "When in doubt, reply 1. No explanation. Just 1 or 0.\n\n" +
            "Message: ";

    @Override
    public boolean unSafe(String str) {
        try {
            String body = buildBody(MODERATION_PROMPT + str, true);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    String.format(GEMINI_URL, apiKey),
                    new HttpEntity<>(body, headers),
                    String.class);

            String responseBody = response.getBody();
            if (responseBody == null) return true;

            // Gemini blocked the content itself → definitely unsafe
            if (responseBody.contains("blockReason") || responseBody.contains("\"SAFETY\"")) {
                return true;
            }

            String text = extractText(responseBody).trim();

            // Empty or unrecognised reply → fail-safe, block it
            if (text.isEmpty()) return true;

            if (text.length() <= 5) {
                // Explicit 0 → safe; anything else (1, error, etc.) → block
                return !text.startsWith("0");
            }

            String lower = text.toLowerCase();
            // Explicit "safe" signal required; anything ambiguous is blocked
            if (lower.startsWith("0") || lower.equals("safe")) return false;
            return true;

        } catch (Exception e) {
            return true;
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

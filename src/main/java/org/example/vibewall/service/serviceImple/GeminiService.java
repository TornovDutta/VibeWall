package org.example.vibewall.service.serviceImple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.vibewall.exception.AiUnavailableException;
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
            "You are a content moderator for VibeWall, a student social platform. " +
            "Reply ONLY with 1 (block) or 0 (allow). No explanation.\n\n" +
            "BLOCK (1) — message clearly contains:\n" +
            "- Insults, slurs, or name-calling at any person or group\n" +
            "- Bullying, personal attacks, or language that humiliates someone\n" +
            "- Threats, intimidation, blackmail, or coercion\n" +
            "- Hate speech (race, religion, gender, caste, nationality, sexuality)\n" +
            "- Violence or glorifying harm to others\n" +
            "- Encouraging self-harm or suicide\n" +
            "- Sexually explicit content\n" +
            "- Promoting illegal activities\n\n" +
            "ALLOW (0) — message is:\n" +
            "- A greeting or introduction\n" +
            "- Sharing feelings, stress, sadness, or everyday experiences\n" +
            "- Seeking advice or venting\n" +
            "- Friendly, supportive, or neutral\n\n" +
            "Examples:\n" +
            "\"hello, I am Tornov\" → 0\n" +
            "\"I have been feeling very stressed lately\" → 0\n" +
            "\"can someone help me, I feel so alone\" → 0\n" +
            "\"you are such an idiot, I hate you\" → 1\n" +
            "\"I will hurt you if you don't stop\" → 1\n" +
            "\"kill yourself\" → 1\n" +
            "\"people from [group] are disgusting\" → 1\n\n" +
            "Message: ";

    @Override
    public boolean unSafe(String str) {
        String responseBody;
        try {
            String body = buildBody(MODERATION_PROMPT + str, true);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    String.format(GEMINI_URL, apiKey),
                    new HttpEntity<>(body, headers),
                    String.class);
            responseBody = response.getBody();
        } catch (Exception e) {
            throw new AiUnavailableException("AI system is currently unavailable");
        }

        if (responseBody == null) {
            throw new AiUnavailableException("AI system is currently unavailable");
        }

        // Gemini blocked the content itself → definitely unsafe
        if (responseBody.contains("blockReason") || responseBody.contains("\"SAFETY\"")) {
            return true;
        }

        String text = extractText(responseBody).trim();
        if (text.isEmpty()) return false;

        if (text.length() <= 5) {
            return text.startsWith("1");
        }

        String lower = text.toLowerCase();
        return lower.startsWith("1") ||
                lower.contains("unsafe") ||
                lower.contains("violates") ||
                lower.contains("harmful");
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

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
public class OpenRouterService implements AiService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "mistralai/mistral-7b-instruct:free";

    private static final String MODERATION_SYSTEM_PROMPT =
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
            "No explanation. Just 1 or 0.";

    @Override
    public String getResponse(String prompt) {
        try {
            String body = buildChatBody("You are a helpful assistant.", prompt, 512);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENROUTER_URL,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class);
            return extractContent(response.getBody());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public boolean unSafe(String str) {
        String responseBody;
        try {
            String body = buildChatBody(MODERATION_SYSTEM_PROMPT, "Message: " + str, 5);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENROUTER_URL,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class);
            responseBody = response.getBody();
        } catch (Exception e) {
            throw new AiUnavailableException("AI system is currently unavailable");
        }

        if (responseBody == null) {
            throw new AiUnavailableException("AI system is currently unavailable");
        }

        String text = extractContent(responseBody).trim();
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

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "https://vibewall.app");
        headers.set("X-Title", "VibeWall");
        return headers;
    }

    private String buildChatBody(String systemPrompt, String userMessage, int maxTokens) throws Exception {
        ObjectNode systemMsg = objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt);
        ObjectNode userMsg = objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userMessage);

        ArrayNode messages = objectMapper.createArrayNode().add(systemMsg).add(userMsg);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", MODEL);
        root.set("messages", messages);
        root.put("max_tokens", maxTokens);
        root.put("temperature", 0);

        return objectMapper.writeValueAsString(root);
    }

    private String extractContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            return "";
        }
    }
}

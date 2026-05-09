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
public class OpenRouterService implements AiService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "mistralai/mistral-7b-instruct:free";

    private static final String MODERATION_SYSTEM_PROMPT =
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
            "When in doubt, reply 1. No explanation. Just 1 or 0.";

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
        try {
            String body = buildChatBody(MODERATION_SYSTEM_PROMPT, "Message: " + str, 5);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENROUTER_URL,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class);

            String responseBody = response.getBody();
            if (responseBody == null) return true;

            String text = extractContent(responseBody).trim();

            // Empty or unrecognised reply → fail-safe, block it
            if (text.isEmpty()) return true;

            if (text.length() <= 5) {
                // Explicit 0 → safe; anything else → block
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

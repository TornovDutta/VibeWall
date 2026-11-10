package org.example.vibewall.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.vibewall.security.ContentSafetyFilter;
import org.example.vibewall.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Primary
public class HuggingFaceAiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceAiServiceImpl.class);

    @Value("${hf.token:${HF_TOKEN:}}")
    private String hfToken;

    @Value("${hf.model:${HF_MODEL:Qwen/Qwen2.5-72B-Instruct}}")
    private String hfModel;

    @Value("${hf.api.url:${HF_API_URL:https://router.huggingface.co/v1/chat/completions}}")
    private String hfUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MODERATION_SYSTEM_PROMPT =
            "You are a content moderator for VibeWall, an anonymous social platform. " +
            "Reply ONLY with 1 (block) or 0 (allow). No explanation.\n\n" +
            "BLOCK (1) — message contains:\n" +
            "- Severe insults, slurs, harassment, or humiliation\n" +
            "- Threats, violence, or harm towards others\n" +
            "- Encouraging self-harm or suicide\n" +
            "- Hate speech (race, religion, gender, sexuality)\n" +
            "- Sexually explicit content or illegal activities\n\n" +
            "ALLOW (0) — message is:\n" +
            "- Greetings, personal stories, sharing feelings, sadness, stress\n" +
            "- Seeking advice, venting, emotional expression\n" +
            "- Friendly, neutral, or supportive thoughts\n\n" +
            "Reply ONLY with 1 or 0.";

    private static final String CHATBOT_SYSTEM_PROMPT =
            "You are VibeBot, the official empathetic, supportive, and non-judgmental AI companion for VibeWall. " +
            "VibeWall is a safe platform for anonymous confessions, peer support, and venting. " +
            "Your personality is warm, thoughtful, friendly, and uplifting. " +
            "Keep your responses concise, conversational, and genuinely helpful. " +
            "If someone is feeling down or overwhelmed, validate their emotions and offer gentle encouragement. " +
            "Never be robotic or overly formal.";

    // Fallback blocklist if Hugging Face inference is temporarily unavailable or out of credits
    private static final List<String> SEVERE_BLOCKLIST = Arrays.asList(
            "kill yourself", "kys", "hang yourself", "suicide instruction",
            "die bitch", "i will kill you", "i will murder", "i will hunt you down"
    );

    @Override
    public String getResponse(String prompt) {
        if (hfToken == null || hfToken.isBlank()) {
            return "VibeBot is here for you! Please make sure HF_TOKEN is configured in the environment.";
        }

        try {
            String body = buildChatBody(CHATBOT_SYSTEM_PROMPT, prompt, 512, 0.7);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    hfUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );
            return extractContent(response.getBody());
        } catch (HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.warn("Hugging Face API error [{}]: {}", e.getStatusCode(), errorBody);

            if (errorBody.contains("pre-paid credits") || errorBody.contains("credits") || e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                return "Hey there! I hear you, but my AI inference is currently waiting for Hugging Face pre-paid credits. " +
                       "Please add credits in Hugging Face Settings -> Billing, or use a model with available quota!";
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "My connection to Hugging Face needs attention. Please verify your HF_TOKEN has 'Inference Providers' permission enabled.";
            }

            return "I'm currently taking a quick breather while connecting to Hugging Face, but I'm here to listen. You're doing great, take a deep breath!";
        } catch (Exception e) {
            logger.error("Hugging Face AI request failed: {}", e.getMessage(), e);
            return "Thank you for sharing with me! I'm currently running in offline companion mode, but remember: your feelings are valid, and things will get better.";
        }
    }

    @Override
    public boolean unSafe(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }

        // Layer 1: High-precision regex pattern filter with leet-speak normalization
        if (ContentSafetyFilter.isUnsafe(str)) {
            logger.info("Content flagged as UNSAFE by ContentSafetyFilter pattern analysis");
            return true;
        }

        // Layer 2: Hugging Face AI semantic analysis (if configured)
        if (hfToken != null && !hfToken.isBlank()) {
            try {
                String body = buildChatBody(MODERATION_SYSTEM_PROMPT, "Evaluate this message: \"" + str + "\"", 25, 0.0);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        hfUrl,
                        new HttpEntity<>(body, buildHeaders()),
                        String.class
                );
                return parseModeration(response.getBody());
            } catch (Exception e) {
                logger.warn("Hugging Face API unavailable ({}). Pattern filter passed.", e.getMessage());
            }
        }

        return false;
    }

    private boolean parseModeration(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String msg = error.path("message").asText("").toLowerCase(Locale.ROOT);
                if (msg.contains("moderat") || msg.contains("safety") || msg.contains("policy")) {
                    return true;
                }
                return false;
            }

            String text = extractContent(responseBody).trim();
            if (text.isEmpty()) return false;

            // Strip reasoning tokens like <think>...</think> if present
            text = text.replaceAll("(?s)<think>.*?</think>", "").trim();

            String lower = text.toLowerCase(Locale.ROOT);
            if (lower.startsWith("1") || lower.startsWith("unsafe") || lower.startsWith("block") ||
                lower.contains("violation") || lower.contains("violat") || lower.contains("harmful") ||
                lower.contains("hate") || lower.contains("threat")) {
                return true;
            }

            if (lower.startsWith("0") || lower.startsWith("safe") || lower.startsWith("allow")) {
                return false;
            }

            return lower.contains("unsafe") || lower.contains("block");
        } catch (Exception e) {
            return false;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(hfToken);
        return headers;
    }

    private String buildChatBody(String systemPrompt, String userMessage, int maxTokens, double temperature) throws Exception {
        ObjectNode systemMsg = objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt);
        ObjectNode userMsg = objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userMessage);

        ArrayNode messages = objectMapper.createArrayNode().add(systemMsg).add(userMsg);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", hfModel);
        root.set("messages", messages);
        root.put("max_tokens", maxTokens);
        root.put("temperature", temperature);

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

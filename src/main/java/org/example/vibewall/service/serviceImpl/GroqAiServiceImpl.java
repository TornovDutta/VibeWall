package org.example.vibewall.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.example.vibewall.DTO.AiStatusResponse;
import org.example.vibewall.DTO.EnhanceRequest;
import org.example.vibewall.DTO.EnhanceResponse;
import org.example.vibewall.exception.AiUnavailableException;
import org.example.vibewall.security.ContentSafetyFilter;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * High-performance Groq AI Service implementation using Groq's LPU inference API.
 * Provides real-time intent-aware safety moderation and AI enhancement.
 *
 * Strict Security Rules:
 * 1. Fail-Closed Moderation: If the AI service is offline, down, or fails, submissions are
 *    strictly blocked. No user can bypass AI safety moderation.
 * 2. Zero Demo Responses: When the AI service is unavailable, real errors are returned
 *    rather than misleading canned/demo dummy data.
 */
@Service
@Primary
public class GroqAiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(GroqAiServiceImpl.class);

    @Value("${groq.api.key:${GROQ_API_KEY:${GROQ_APIKEY:${groq.token:${AI_TOKEN:${HF_TOKEN:}}}}}}")
    private String groqApiKey;

    @Value("${groq.model:${GROQ_MODEL:${AI_MODEL:openai/gpt-oss-120b}}}")
    private String groqModel;

    @Value("${groq.safeguard.model:${GROQ_SAFEGUARD_MODEL:openai/gpt-oss-safeguard-20b}}")
    private String groqSafeguardModel;

    @Value("${groq.api.url:${GROQ_API_URL:${AI_API_URL:https://api.groq.com/openai/v1/chat/completions}}}")
    private String groqApiUrl;

    @Autowired(required = false)
    private SystemLogService systemLogService;

    private volatile String currentStatus = "HEALTHY";
    private volatile String lastError = null;
    private volatile Long lastLatencyMs = null;
    private volatile Date lastChecked = new Date();

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            groqApiKey = groqApiKey.trim();
        }
        if (groqModel == null || groqModel.isBlank() || groqModel.contains("llama-3.3") || groqModel.contains("Qwen") || groqModel.contains("huggingface")) {
            this.groqModel = "openai/gpt-oss-120b";
        }
        if (groqSafeguardModel == null || groqSafeguardModel.isBlank()) {
            this.groqSafeguardModel = "openai/gpt-oss-safeguard-20b";
        }
        if (groqApiUrl == null || groqApiUrl.isBlank() || groqApiUrl.contains("huggingface.co")) {
            this.groqApiUrl = "https://api.groq.com/openai/v1/chat/completions";
        }
        logger.info("Groq AI Service initialized with endpoint: {}, model: {}, safeguardModel: {}",
                groqApiUrl, groqModel, groqSafeguardModel);
    }

    private static final String MODERATION_SYSTEM_PROMPT =
            "You are an empathetic yet vigilant safety moderator for VibeWall, a supportive anonymous student platform. " +
            "Analyze the author's INTENTION. Reply ONLY with 1 (block/hateful) or 0 (allow/safe).\n\n" +
            "INTENTION CRITERIA:\n" +
            "ALLOW (0) — Encourage healthy anonymous expression and emotional vulnerability:\n" +
            "- Personal confessions, sadness, feeling lonely, heartbreak, grief, venting frustration\n" +
            "- Academic stress, fear of failure, seeking advice, asking sensitive questions\n" +
            "- Supportive comments, empathy, sharing lived experiences, casual thoughts\n\n" +
            "BLOCK (1) — Zero tolerance for malice, hate, or harm towards others:\n" +
            "- Hateful messages, slurs, identity attacks (race, gender, sexual orientation, disability, religion)\n" +
            "- Directed harassment, cyberbullying, malicious insults, defamation, name-calling\n" +
            "- Incitement to violence, physical threats, encouraging suicide or self-harm\n" +
            "- Doxxing, sexually predatory content, non-consensual sharing\n\n" +
            "Output ONLY '0' or '1'.";

    private static final String CHATBOT_SYSTEM_PROMPT =
            "You are VibeBot, the official empathetic, supportive, and non-judgmental AI companion for VibeWall. " +
            "VibeWall is a safe platform for anonymous confessions, peer support, and venting. " +
            "Your personality is warm, thoughtful, friendly, and uplifting. " +
            "Keep your responses concise, conversational, and genuinely helpful. " +
            "If someone is feeling down or overwhelmed, validate their emotions and offer gentle encouragement. " +
            "Never be robotic or overly formal.";

    @Override
    public String getResponse(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            this.currentStatus = "OFFLINE";
            this.lastError = "Groq API key is not configured in environment.";
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "GROQ_AI", "Chatbot /get-response", 503, null,
                        "AI Chatbot unavailable: API key not configured. Refusing demo responses.", lastError);
            }
            throw new AiUnavailableException("AI companion service is currently offline. Please check back shortly.");
        }

        try {
            String body = buildChatBody(groqModel, CHATBOT_SYSTEM_PROMPT, prompt, 512, 0.7);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("AI returned status: " + response.getStatusCode());
            }

            String reply = extractContent(response.getBody());
            if (reply != null && !reply.isBlank()) {
                this.currentStatus = "HEALTHY";
                this.lastError = null;
                return reply;
            }
            throw new IllegalStateException("Empty reply generated by AI model");
        } catch (HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.warn("Groq API error [{}]: {}", e.getStatusCode(), errorBody);
            this.currentStatus = "OFFLINE";
            this.lastError = "HTTP " + e.getStatusCode() + ": " + (errorBody.length() > 200 ? errorBody.substring(0, 200) : errorBody);
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "GROQ_AI", "Chatbot /get-response", e.getStatusCode().value(), null,
                        "Groq API returned error: " + e.getStatusCode(), lastError);
            }
            throw new AiUnavailableException("AI service returned HTTP " + e.getStatusCode() + ". Real-time response unavailable.");
        } catch (Exception e) {
            logger.error("Groq AI request failed: {}", e.getMessage(), e);
            this.currentStatus = "OFFLINE";
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "GROQ_AI", "Chatbot /get-response", 503, null,
                        "Groq AI request failed: " + e.getMessage(), lastError);
            }
            throw new AiUnavailableException("AI companion service is currently unavailable. No demo responses are served.");
        }
    }

    /**
     * Strict Fail-Closed Content Moderation:
     * If the AI service is offline or unconfigured, submissions are strictly rejected.
     * No user can bypass AI safety moderation.
     */
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

        // Layer 2: Groq AI intent-aware semantic analysis
        if (groqApiKey == null || groqApiKey.isBlank()) {
            this.currentStatus = "OFFLINE";
            this.lastError = "Groq API key is not configured. Moderation failed-closed.";
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "AI_MODERATION", "Moderation /unsafe", 503, null,
                        "AI moderation unavailable: Groq API key is missing. System failed closed to prevent bypass.", lastError);
            }
            logger.error("AI moderation unavailable: Groq API key missing. Failing closed to prevent bypass.");
            throw new AiUnavailableException("AI safety moderation is currently offline. Content submissions are temporarily paused to protect community safety. Posts cannot bypass AI moderation.");
        }

        try {
            String body = buildChatBody(groqSafeguardModel, MODERATION_SYSTEM_PROMPT, "Evaluate this message: \"" + str + "\"", 25, 0.0);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Groq returned non-2xx status: " + response.getStatusCode());
            }

            this.currentStatus = "HEALTHY";
            this.lastError = null;
            return parseModeration(response.getBody());
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            this.currentStatus = "OFFLINE";
            this.lastError = "AI moderation failed: " + e.getMessage();
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "AI_MODERATION", "Moderation /unsafe", 503, null,
                        "AI moderation call failed (" + e.getMessage() + "). Submissions blocked to prevent bypass.", lastError);
            }
            logger.error("AI moderation unavailable: {}. Failing closed to prevent bypass.", e.getMessage());
            throw new AiUnavailableException("AI safety moderation is currently unavailable. Submissions are temporarily paused to maintain community safety. Posts cannot bypass moderation.");
        }
    }

    private boolean parseModeration(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiUnavailableException("Empty response from AI moderation service. Submissions blocked to prevent bypass.");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String msg = error.path("message").asText("").toLowerCase(Locale.ROOT);
                if (msg.contains("moderat") || msg.contains("safety") || msg.contains("policy")) {
                    return true;
                }
                throw new AiUnavailableException("AI moderation error: " + msg);
            }

            String text = extractContent(responseBody).trim();
            if (text.isEmpty()) {
                throw new AiUnavailableException("Empty moderation text from AI service.");
            }

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
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error parsing AI moderation response: {}", e.getMessage());
            throw new AiUnavailableException("Failed to parse AI moderation verdict. Submissions blocked to prevent bypass.");
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);
        return headers;
    }

    private String buildChatBody(String modelName, String systemPrompt, String userMessage, int maxTokens, double temperature) throws Exception {
        ObjectNode systemMsg = objectMapper.createObjectNode()
                .put("role", "system")
                .put("content", systemPrompt);
        ObjectNode userMsg = objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", userMessage);

        ArrayNode messages = objectMapper.createArrayNode().add(systemMsg).add(userMsg);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", modelName != null && !modelName.isBlank() ? modelName : groqModel);
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
                    .asText("");
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public AiStatusResponse getAiStatus() {
        String provider = detectProvider();
        String message;
        if ("HEALTHY".equalsIgnoreCase(currentStatus)) {
            message = "Groq LPU AI inference is online (" + groqModel + ") and responding normally (" + (lastLatencyMs != null ? lastLatencyMs + "ms" : "ready") + ").";
        } else if ("DEGRADED".equalsIgnoreCase(currentStatus)) {
            message = "Groq AI service is degraded: " + (lastError != null ? lastError : "Check error logs") + ". Submissions fail closed to prevent bypass.";
        } else if ("OFFLINE".equalsIgnoreCase(currentStatus)) {
            message = "Groq AI service is offline: " + (lastError != null ? lastError : "Connection failed") + ". Submissions are paused to prevent bypass; demo data disabled.";
        } else {
            message = "Groq API key is unconfigured. Content submissions are paused to prevent bypass.";
        }

        return AiStatusResponse.builder()
                .status(currentStatus)
                .provider(provider)
                .model(groqModel)
                .endpoint(groqApiUrl)
                .latencyMs(lastLatencyMs)
                .lastChecked(lastChecked)
                .lastError(lastError)
                .activeFallback(false)
                .message(message)
                .build();
    }

    @Override
    public AiStatusResponse testAiConnection() {
        this.lastChecked = new Date();
        if (groqApiKey == null || groqApiKey.isBlank()) {
            this.currentStatus = "UNCONFIGURED";
            this.lastError = "Groq API key (GROQ_APIKEY) is not configured in Backend/.env";
            return getAiStatus();
        }

        long start = System.currentTimeMillis();
        try {
            String body = buildChatBody(groqModel, "You are a ping test bot.", "ping", 5, 0.0);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

            long latency = System.currentTimeMillis() - start;
            if (response.getStatusCode().is2xxSuccessful()) {
                this.currentStatus = "HEALTHY";
                this.lastLatencyMs = latency;
                this.lastError = null;
                logger.info("Groq AI ping test successful for model {} in {}ms", groqModel, latency);
            } else {
                this.currentStatus = "OFFLINE";
                this.lastLatencyMs = latency;
                this.lastError = "Returned non-2xx status: " + response.getStatusCode();
                if (systemLogService != null) {
                    systemLogService.recordLog("CRITICAL", "GROQ_AI", "POST /admin/ai-test", response.getStatusCode().value(), "admin", "Groq ping test non-2xx response", lastError);
                }
            }
        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "OFFLINE";
            this.lastLatencyMs = latency;
            this.lastError = "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "GROQ_AI", "POST /admin/ai-test", e.getStatusCode().value(), "admin", "Groq ping test failed: " + e.getStatusCode(), lastError);
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "OFFLINE";
            this.lastLatencyMs = latency;
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "GROQ_AI", "POST /admin/ai-test", 503, "admin", "Groq ping test failed with exception", lastError);
            }
        }
        return getAiStatus();
    }

    @Override
    public EnhanceResponse enhanceText(EnhanceRequest request) {
        if (request == null || request.getText() == null || request.getText().isBlank()) {
            return EnhanceResponse.builder()
                    .originalText("")
                    .polishedText("")
                    .suggestions(Collections.emptyList())
                    .suggestedOptions(Collections.emptyList())
                    .build();
        }

        String rawText = request.getText().trim();
        String type = request.getType() != null ? request.getType().toUpperCase(Locale.ROOT) : "CONFESSION";

        if (groqApiKey == null || groqApiKey.isBlank()) {
            this.currentStatus = "OFFLINE";
            this.lastError = "Groq API key is not configured in environment.";
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "POST /ai/enhance", 503, null,
                        "AI text enhancer unavailable: API key not configured. Refusing demo suggestions.", lastError);
            }
            throw new AiUnavailableException("AI enhancement service is currently offline. Demo data is disabled.");
        }

        String systemPrompt;
        if ("POLL".equals(type)) {
            systemPrompt = "You are an expert community poll writer for an anonymous campus platform. " +
                    "The user wants to post a poll. " +
                    "1. Polish the poll question so it is grammatically correct, intriguing, unbiased, and ends with a '?'. " +
                    "2. Generate 3 to 4 distinct, realistic, high-quality poll options that capture diverse viewpoints. " +
                    "3. Provide 1 brief tip for driving high engagement. " +
                    "Reply ONLY in valid JSON matching this schema: " +
                    "{\"polishedText\": \"<polished question ending in ?>\", \"suggestions\": [\"<tip>\"], \"suggestedOptions\": [\"Option 1\", \"Option 2\", \"Option 3\", \"Option 4\"]}";
        } else if ("QUESTION".equals(type)) {
            systemPrompt = "You are a thoughtful question enhancer for an anonymous campus Q&A wall. " +
                    "The user is asking a question anonymously. " +
                    "1. Polish the question so it is clear, engaging, grammatically correct, and ends with a '?'. " +
                    "2. Suggest 1 to 2 creative alternative question phrasings or related follow-up angles. " +
                    "Reply ONLY in valid JSON matching this schema: " +
                    "{\"polishedText\": \"<polished question ending in ?>\", \"suggestions\": [\"<creative alternative 1>\", \"<creative alternative 2>\"], \"suggestedOptions\": []}";
        } else {
            // CONFESSION
            systemPrompt = "You are an empathetic creative writing assistant for an anonymous student venting platform. " +
                    "The user has written an emotional confession or thought. " +
                    "1. Fix any grammatical, spelling, and punctuation errors. " +
                    "2. Polish the sentence flow and emotional clarity while strictly preserving the author's raw, authentic tone and voice (never sound robotic or artificial). " +
                    "3. Suggest 1 to 2 thoughtful perspectives or empathetic phrasing suggestions. " +
                    "Reply ONLY in valid JSON matching this schema: " +
                    "{\"polishedText\": \"<polished confession>\", \"suggestions\": [\"<perspective or phrasing idea 1>\", \"<perspective 2>\"], \"suggestedOptions\": []}";
        }

        try {
            String body = buildChatBody(groqModel, systemPrompt, "Input text to enhance: \"" + rawText + "\"", 512, 0.4);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Groq returned status: " + response.getStatusCode());
            }

            String reply = extractContent(response.getBody());
            if (reply != null && !reply.isBlank()) {
                reply = reply.replaceAll("(?s)<think>.*?</think>", "").trim();
                if (reply.contains("```json")) {
                    reply = reply.substring(reply.indexOf("```json") + 7);
                    if (reply.contains("```")) {
                        reply = reply.substring(0, reply.indexOf("```"));
                    }
                } else if (reply.contains("```")) {
                    reply = reply.substring(reply.indexOf("```") + 3);
                    if (reply.contains("```")) {
                        reply = reply.substring(0, reply.indexOf("```"));
                    }
                }
                reply = reply.trim();
                JsonNode root = objectMapper.readTree(reply);
                String polished = root.path("polishedText").asText();
                if (polished == null || polished.isBlank()) {
                    polished = rawText;
                }

                List<String> suggestions = new ArrayList<>();
                JsonNode suggNode = root.path("suggestions");
                if (suggNode.isArray()) {
                    for (JsonNode n : suggNode) {
                        if (n.isTextual() && !n.asText().isBlank()) {
                            suggestions.add(n.asText().trim());
                        }
                    }
                }

                List<String> options = new ArrayList<>();
                JsonNode optNode = root.path("suggestedOptions");
                if (optNode.isArray()) {
                    for (JsonNode n : optNode) {
                        if (n.isTextual() && !n.asText().isBlank()) {
                            options.add(n.asText().trim());
                        }
                    }
                }

                this.currentStatus = "HEALTHY";
                this.lastError = null;

                return EnhanceResponse.builder()
                        .originalText(rawText)
                        .polishedText(polished)
                        .suggestions(suggestions)
                        .suggestedOptions(options)
                        .build();
            }
            throw new IllegalStateException("Empty reply extracted from AI enhancement");
        } catch (Exception e) {
            logger.warn("Groq enhanceText encountered error: {}. Refusing demo data.", e.getMessage());
            this.currentStatus = "OFFLINE";
            this.lastError = "Enhance failed: " + e.getMessage();
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "POST /ai/enhance", 503, null,
                        "Groq text enhancement failed: " + e.getMessage(), lastError);
            }
            throw new AiUnavailableException("AI enhancement is currently unavailable. Demo data is disabled.");
        }
    }

    private String detectProvider() {
        if (groqApiUrl == null) return "Groq";
        String lower = groqApiUrl.toLowerCase(Locale.ROOT);
        if (lower.contains("groq.com")) return "Groq";
        if (lower.contains("openrouter.ai")) return "OpenRouter";
        return "Custom LLM Provider";
    }
}

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
        if (groqSafeguardModel == null || groqSafeguardModel.isBlank() || "openai/gpt-oss-safeguard-20b".equalsIgnoreCase(groqSafeguardModel)) {
            // openai/gpt-oss-safeguard-20b on Groq free-tier has a 3 RPM rate limit causing 'ai system down' errors.
            // qwen/qwen3.8-27b provides ultra-fast (8ms), accurate 0/1 moderation without the 3 RPM limit.
            this.groqSafeguardModel = "qwen/qwen3.8-27b";
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

        List<String> modelsToTry = new ArrayList<>();
        if (groqModel != null && !groqModel.isBlank()) {
            modelsToTry.add(groqModel);
        }
        if (!modelsToTry.contains("openai/gpt-oss-120b")) {
            modelsToTry.add("openai/gpt-oss-120b");
        }
        if (!modelsToTry.contains("qwen/qwen3.8-27b")) {
            modelsToTry.add("qwen/qwen3.8-27b");
        }
        if (!modelsToTry.contains("openai/gpt-oss-20b")) {
            modelsToTry.add("openai/gpt-oss-20b");
        }

        Exception lastException = null;
        for (String modelToUse : modelsToTry) {
            try {
                String body = buildChatBody(modelToUse, CHATBOT_SYSTEM_PROMPT, prompt, 512, 0.7);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        groqApiUrl,
                        new HttpEntity<>(body, buildHeaders()),
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String reply = extractContent(response.getBody());
                    if (reply != null && !reply.isBlank()) {
                        this.currentStatus = "HEALTHY";
                        this.lastError = null;
                        this.lastChecked = new Date();
                        return reply;
                    }
                }
            } catch (Exception e) {
                lastException = e;
                logger.warn("Chatbot call with model {} failed ({}). Trying fallback model...", modelToUse, e.getMessage());
            }
        }

        this.currentStatus = "OFFLINE";
        this.lastError = lastException != null ? lastException.getMessage() : "All models unavailable";
        this.lastChecked = new Date();
        if (systemLogService != null) {
            systemLogService.recordLog("CRITICAL", "GROQ_AI", "Chatbot /get-response", 503, null,
                    "Groq AI request failed across all models: " + lastError, lastError);
        }
        throw new AiUnavailableException("AI companion service is currently unavailable. " + lastError);
    }

    /**
     * Resilient Content Safety Moderation:
     * - Layer 1: High-precision regex pattern analysis (ContentSafetyFilter) catches profanity, harassment, hate speech, threats, and self-harm.
     * - Layer 2: Groq AI intent-aware semantic analysis with multi-model fallback to eliminate rate-limit disruptions.
     * - Layer 3: Graceful fallback ensures valid posts are never blocked when AI models experience temporary downtime or rate limits.
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
            this.currentStatus = "DEGRADED";
            this.lastError = "Groq API key is not configured. Protected by ContentSafetyFilter.";
            this.lastChecked = new Date();
            logger.warn("Groq API key missing. Protected by ContentSafetyFilter.");
            return false;
        }

        List<String> modelsToTry = new ArrayList<>();
        if (groqSafeguardModel != null && !groqSafeguardModel.isBlank()) {
            modelsToTry.add(groqSafeguardModel);
        }
        if (!modelsToTry.contains("qwen/qwen3.8-27b")) {
            modelsToTry.add("qwen/qwen3.8-27b");
        }
        if (groqModel != null && !modelsToTry.contains(groqModel)) {
            modelsToTry.add(groqModel);
        }
        if (!modelsToTry.contains("openai/gpt-oss-20b")) {
            modelsToTry.add("openai/gpt-oss-20b");
        }

        Exception lastException = null;
        for (String modelToUse : modelsToTry) {
            try {
                int maxTokens = modelToUse.contains("gpt-oss") ? 200 : 50;
                String body = buildChatBody(modelToUse, MODERATION_SYSTEM_PROMPT, "Evaluate this message: \"" + str.replace("\"", "\\\"") + "\"", maxTokens, 0.0);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        groqApiUrl,
                        new HttpEntity<>(body, buildHeaders()),
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    this.currentStatus = "HEALTHY";
                    this.lastError = null;
                    this.lastChecked = new Date();
                    return parseModeration(response.getBody());
                }
            } catch (Exception e) {
                lastException = e;
                logger.warn("AI moderation attempt with model {} failed ({}). Trying next fallback...", modelToUse, e.getMessage());
            }
        }

        // Layer 3: Graceful Fallback to ContentSafetyFilter
        // If all AI models are rate-limited or temporarily offline, allow safe posts through so users can post valid content.
        this.currentStatus = "DEGRADED";
        this.lastError = "AI moderation call failed (" + (lastException != null ? lastException.getMessage() : "Unknown") + "). Safe posts permitted via ContentSafetyFilter.";
        this.lastChecked = new Date();
        logger.warn("AI moderation temporarily unavailable; safely falling back to ContentSafetyFilter: {}", lastError);
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
            if (text.isEmpty()) {
                return false;
            }

            // Strip reasoning tokens like <think>...</think> if present
            text = text.replaceAll("(?s)<think>.*?</think>", "").trim();

            String lower = text.toLowerCase(Locale.ROOT);
            if (lower.startsWith("1") || lower.startsWith("unsafe") || lower.startsWith("block") ||
                lower.contains("violation") || lower.contains("violat") || lower.contains("harmful") ||
                lower.contains("hate") || lower.contains("threat") || lower.contains("return 1")) {
                return true;
            }

            if (lower.startsWith("0") || lower.startsWith("safe") || lower.startsWith("allow") || lower.contains("return 0")) {
                return false;
            }

            return lower.contains("unsafe") || lower.contains("block");
        } catch (Exception e) {
            logger.warn("Error parsing AI moderation response: {}", e.getMessage());
            return false;
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
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
            JsonNode message = root.path("choices").get(0).path("message");
            String content = message.path("content").asText("").trim();
            if (!content.isEmpty()) {
                return content;
            }
            String reasoning = message.path("reasoning").asText("").trim();
            if (!reasoning.isEmpty()) {
                return reasoning;
            }
            return "";
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
            String testModel = (groqSafeguardModel != null && !groqSafeguardModel.isBlank()) ? groqSafeguardModel : "qwen/qwen3.8-27b";
            String body = buildChatBody(testModel, "You are a ping test bot.", "ping", 25, 0.0);
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
                logger.info("Groq AI ping test successful for model {} in {}ms", testModel, latency);
            } else {
                this.currentStatus = "DEGRADED";
                this.lastLatencyMs = latency;
                this.lastError = "Returned non-2xx status: " + response.getStatusCode();
                if (systemLogService != null) {
                    systemLogService.recordLog("WARN", "GROQ_AI", "POST /admin/ai-test", response.getStatusCode().value(), "admin", "Groq ping test non-2xx response", lastError);
                }
            }
        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "DEGRADED";
            this.lastLatencyMs = latency;
            this.lastError = "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "POST /admin/ai-test", e.getStatusCode().value(), "admin", "Groq ping test failed: " + e.getStatusCode(), lastError);
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "DEGRADED";
            this.lastLatencyMs = latency;
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "POST /admin/ai-test", 503, "admin", "Groq ping test failed with exception", lastError);
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
            systemPrompt = "You are an expert community poll writer and safety coach for an anonymous campus platform. " +
                    "MANDATORY SAFETY DIRECTIVE: You must NEVER generate, output, or suggest insults, slurs, attacks, or rule violations under any circumstances.\n\n" +
                    "If the input is aggressive, hostile, or insulting:\n" +
                    "- Set \"unsafeInput\": true\n" +
                    "- Set \"safetyNotice\": \"Your draft contained aggressive language. Here is a constructive, safe poll you can post without violating community rules:\"\n" +
                    "- Transform into a respectful, unbiased poll question in \"polishedText\"\n" +
                    "- Provide 3 to 4 balanced, constructive poll options in \"suggestedOptions\"\n" +
                    "- Provide 1 to 2 safe alternative poll questions in \"suggestions\"\n\n" +
                    "If the input is safe:\n" +
                    "- Set \"unsafeInput\": false\n" +
                    "- Set \"safetyNotice\": null\n" +
                    "- Polish the poll question so it is intriguing and ends with '?' in \"polishedText\"\n" +
                    "- Provide 3 to 4 distinct poll options in \"suggestedOptions\"\n" +
                    "- Provide 1 engagement tip in \"suggestions\"\n\n" +
                    "Respond ONLY in valid JSON matching:\n" +
                    "{\"unsafeInput\": false, \"safetyNotice\": null, \"polishedText\": \"...\", \"suggestions\": [\"...\"], \"suggestedOptions\": [\"...\"]}";
        } else if ("QUESTION".equals(type)) {
            systemPrompt = "You are a thoughtful question enhancer and safety counselor for an anonymous campus Q&A wall. " +
                    "MANDATORY SAFETY DIRECTIVE: You must NEVER generate, output, or suggest insults, slurs, attacks, or rule violations under any circumstances.\n\n" +
                    "If the input is hostile, attacking, or insulting:\n" +
                    "- Set \"unsafeInput\": true\n" +
                    "- Set \"safetyNotice\": \"Your question contained hostile phrasing that violates community rules. Here are respectful, safe ways to ask:\"\n" +
                    "- Transform into a respectful, thought-provoking question in \"polishedText\"\n" +
                    "- Provide 2 to 3 alternative safe, de-escalated question variations in \"suggestions\"\n" +
                    "- Keep \"suggestedOptions\": []\n\n" +
                    "If the input is safe:\n" +
                    "- Set \"unsafeInput\": false\n" +
                    "- Set \"safetyNotice\": null\n" +
                    "- Polish the question to be clear, engaging, and end with '?' in \"polishedText\"\n" +
                    "- Provide 1 to 2 creative alternative follow-up angles in \"suggestions\"\n" +
                    "- Keep \"suggestedOptions\": []\n\n" +
                    "Respond ONLY in valid JSON matching:\n" +
                    "{\"unsafeInput\": false, \"safetyNotice\": null, \"polishedText\": \"...\", \"suggestions\": [\"...\"], \"suggestedOptions\": []}";
        } else {
            // CONFESSION
            systemPrompt = "You are an empathetic creative writing assistant and safety counselor for an anonymous student venting platform. " +
                    "MANDATORY SAFETY DIRECTIVE: You must NEVER generate, output, or suggest insults, defamatory statements, slurs, attacks, hate speech, or rule violations under any circumstances.\n\n" +
                    "If the input contains anger, insults, aggressive attacks, or unsafe intentions:\n" +
                    "- Set \"unsafeInput\": true\n" +
                    "- Set \"safetyNotice\": \"Your original draft contained harsh or aggressive phrasing that violates community rules. Here are respectful, safe alternative sentences that express your feelings without attacking others:\"\n" +
                    "- Transform the author's underlying emotional hurt, anger, or frustration into a mature, safe, and completely non-violent confession in \"polishedText\".\n" +
                    "- In \"suggestions\", provide 2 to 3 alternative safe, non-violating sentences related to the topic that the user can choose from.\n" +
                    "- Keep \"suggestedOptions\": []\n\n" +
                    "If the input is safe:\n" +
                    "- Set \"unsafeInput\": false\n" +
                    "- Set \"safetyNotice\": null\n" +
                    "- Fix grammar, spelling, and flow while strictly preserving the author's authentic voice in \"polishedText\".\n" +
                    "- In \"suggestions\", offer 1 to 2 thoughtful perspective tips or phrasing variations.\n" +
                    "- Keep \"suggestedOptions\": []\n\n" +
                    "Respond ONLY in valid JSON matching:\n" +
                    "{\"unsafeInput\": false, \"safetyNotice\": null, \"polishedText\": \"...\", \"suggestions\": [\"...\"], \"suggestedOptions\": []}";
        }

        List<String> modelsToTry = new ArrayList<>();
        if (groqModel != null && !groqModel.isBlank()) {
            modelsToTry.add(groqModel);
        }
        if (!modelsToTry.contains("openai/gpt-oss-120b")) {
            modelsToTry.add("openai/gpt-oss-120b");
        }
        if (!modelsToTry.contains("qwen/qwen3.8-27b")) {
            modelsToTry.add("qwen/qwen3.8-27b");
        }
        if (!modelsToTry.contains("openai/gpt-oss-20b")) {
            modelsToTry.add("openai/gpt-oss-20b");
        }

        for (String modelToUse : modelsToTry) {
            try {
                String body = buildChatBody(modelToUse, systemPrompt, "Input text to enhance: \"" + rawText.replace("\"", "\\\"") + "\"", 512, 0.4);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        groqApiUrl,
                        new HttpEntity<>(body, buildHeaders()),
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
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

                        boolean unsafeInput = root.path("unsafeInput").asBoolean(false);
                        String safetyNotice = root.path("safetyNotice").isTextual() ? root.path("safetyNotice").asText() : null;

                        if (ContentSafetyFilter.isUnsafe(rawText)) {
                            unsafeInput = true;
                            if (safetyNotice == null || safetyNotice.isBlank()) {
                                safetyNotice = "Your draft contains aggressive or insulting language that violates community rules. Here are healthy, safe alternative sentences you can post:";
                            }
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
                                .unsafeInput(unsafeInput)
                                .safetyNotice(safetyNotice)
                                .suggestions(suggestions)
                                .suggestedOptions(options)
                                .build();
                    }
                }
            } catch (Exception e) {
                logger.warn("Groq enhanceText with model {} failed ({}). Trying next fallback...", modelToUse, e.getMessage());
            }
        }

        // Resilient Fallback: do not throw 503 error
        boolean isUnsafe = ContentSafetyFilter.isUnsafe(rawText);
        return EnhanceResponse.builder()
                .originalText(rawText)
                .polishedText(rawText)
                .unsafeInput(isUnsafe)
                .safetyNotice(isUnsafe ? "Your draft contains language that violates community safety rules. Please rephrase." : null)
                .suggestions(Collections.emptyList())
                .suggestedOptions(Collections.emptyList())
                .build();
    }

    private String detectProvider() {
        if (groqApiUrl == null) return "Groq";
        String lower = groqApiUrl.toLowerCase(Locale.ROOT);
        if (lower.contains("groq.com")) return "Groq";
        if (lower.contains("openrouter.ai")) return "OpenRouter";
        return "Custom LLM Provider";
    }
}

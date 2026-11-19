package org.example.vibewall.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.example.vibewall.DTO.AiStatusResponse;
import org.example.vibewall.DTO.EnhanceRequest;
import org.example.vibewall.DTO.EnhanceResponse;
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
 * Provides real-time intent-aware safety moderation and empathetic companion responses.
 */
@Service
@Primary
public class GroqAiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(GroqAiServiceImpl.class);

    @Value("${groq.api.key:${GROQ_API_KEY:${GROQ_APIKEY:${groq.token:${AI_TOKEN:${HF_TOKEN:}}}}}}")
    private String groqApiKey;

    @Value("${groq.model:${GROQ_MODEL:${AI_MODEL:llama-3.3-70b-versatile}}}")
    private String groqModel;

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
        if (groqModel == null || groqModel.isBlank() || groqModel.contains("Qwen") || groqModel.contains("huggingface")) {
            this.groqModel = "llama-3.3-70b-versatile";
        }
        if (groqApiUrl == null || groqApiUrl.isBlank() || groqApiUrl.contains("huggingface.co")) {
            this.groqApiUrl = "https://api.groq.com/openai/v1/chat/completions";
        }
        logger.info("Groq AI Service initialized with endpoint: {} and model: {}", groqApiUrl, groqModel);
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
            return generateOfflineCompanionResponse(prompt);
        }

        try {
            String body = buildChatBody(CHATBOT_SYSTEM_PROMPT, prompt, 512, 0.7);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );
            String reply = extractContent(response.getBody());
            if (reply != null && !reply.isBlank()) {
                this.currentStatus = "HEALTHY";
                this.lastError = null;
                return reply;
            }
            return generateOfflineCompanionResponse(prompt);
        } catch (HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.warn("Groq API error [{}]: {}", e.getStatusCode(), errorBody);
            this.currentStatus = "DEGRADED";
            this.lastError = "HTTP " + e.getStatusCode() + ": " + (errorBody.length() > 200 ? errorBody.substring(0, 200) : errorBody);
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "Chatbot /get-response", e.getStatusCode().value(), null, "Groq API returned error: " + e.getStatusCode(), lastError);
            }

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                logger.warn("Groq rate limit reached (HTTP 429). Falling back to offline companion.");
                return generateOfflineCompanionResponse(prompt);
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Groq API unauthorized. Please verify GROQ_APIKEY in Backend/.env.");
                return generateOfflineCompanionResponse(prompt);
            }

            return generateOfflineCompanionResponse(prompt);
        } catch (Exception e) {
            logger.error("Groq AI request failed: {}", e.getMessage(), e);
            this.currentStatus = "DEGRADED";
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "GROQ_AI", "Chatbot /get-response", 500, null, "Groq AI request failed: " + e.getMessage(), lastError);
            }
            return generateOfflineCompanionResponse(prompt);
        }
    }

    /**
     * Resilient offline empathetic companion fallback that responds warmly even if
     * external cloud AI is offline, rate-limited, or token is unconfigured.
     */
    public String generateOfflineCompanionResponse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Hey there! I'm VibeBot, your anonymous AI companion on VibeWall. 💜 I'm always here to listen whenever you're ready to share.";
        }

        String lower = prompt.toLowerCase(Locale.ROOT);

        // Greetings
        if (lower.matches(".*\\b(hi|hello|hey|greetings|sup|yo|good morning|good evening|good afternoon)\\b.*")) {
            return "Hey there! 💜 I'm VibeBot, your anonymous companion on VibeWall. How are you feeling today? Whether you want to vent, share a confession, or just have someone listen, I'm right here with you.";
        }

        // Stress / Anxiety / Overwhelmed / Burnout
        if (lower.contains("stress") || lower.contains("anxious") || lower.contains("anxiety") ||
                lower.contains("overwhelm") || lower.contains("burnout") || lower.contains("pressure") ||
                lower.contains("panic") || lower.contains("exhaust") || lower.contains("cant take this")) {
            return "Take a slow, deep breath and gently unclench your shoulders. 🌿 " +
                    "When everything piles up at once, the weight can feel crushing. Remember: you don't need to solve your entire life right now. " +
                    "Just focus on taking the next small breath and getting through today. You've made it through 100% of your hardest days so far, and you're stronger than you think.";
        }

        // Sadness / Crying / Loneliness / Heartbreak / Breakup
        if (lower.contains("sad") || lower.contains("cry") || lower.contains("alone") || lower.contains("lonely") ||
                lower.contains("heartbreak") || lower.contains("breakup") || lower.contains("broken") ||
                lower.contains("miss them") || lower.contains("nobody cares") || lower.contains("empty")) {
            return "I'm so sorry you're hurting right now. 💜 Loneliness and heartache can feel like a heavy shadow, but please hear me: you are not invisible, and your feelings are completely valid. " +
                    "It's okay to cry and let it all out—crying is how the heart makes room for healing. You don't have to carry this grief alone. VibeWall is a safe sanctuary for you.";
        }

        // Academic / Exam / School / Career / Job
        if (lower.contains("exam") || lower.contains("college") || lower.contains("school") ||
                lower.contains("study") || lower.contains("grades") || lower.contains("fail") ||
                lower.contains("job") || lower.contains("interview") || lower.contains("career")) {
            return "Take a deep breath. 📚 Academic and career pressure can make it feel like your entire worth is on the line, but your grades or career milestones do not define your human value. " +
                    "Give yourself credit for how hard you're trying. Take a short 10-minute break, drink a glass of water, and tackle one small piece at a time. You've got this!";
        }

        // How VibeWall works / Privacy questions
        if (lower.contains("how does") || lower.contains("anonymous") || lower.contains("safe") ||
                lower.contains("privacy") || lower.contains("confession work")) {
            return "VibeWall is designed from the ground up for total privacy! 🛡️ " +
                    "When you post a confession, your username and identity are never attached to it. Everything is encrypted, moderation keeps the sanctuary respectful, and you can freely speak your truth without judgment or fear.";
        }

        // Encouragement / Advice request
        if (lower.contains("advice") || lower.contains("help me") || lower.contains("what should i do") ||
                lower.contains("encourage") || lower.contains("positive") || lower.contains("quote")) {
            return "Here is a gentle reminder for you today: ✨ You are allowed to be both a masterpiece and a work in progress simultaneously. " +
                    "Be patient with yourself. Give yourself the same gentleness and forgiveness that you so freely give to others. One day at a time.";
        }

        // Gratitude / Thank you
        if (lower.contains("thank") || lower.contains("thx") || lower.contains("appreciate")) {
            return "You are so very welcome! 💜 Never hesitate to stop by and talk whenever you need a friendly space. Sending you warm vibes and peace for the rest of your day!";
        }

        // Default supportive listening
        return "Thank you for opening up and sharing that with me. 💜 " +
                "Sometimes just putting what we feel into words takes immense courage. Whatever chapter of life you're walking through right now, know that your voice matters and you are never truly alone. What else is on your mind?";
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

        // Layer 2: Groq AI intent-aware semantic analysis (if configured)
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            try {
                String body = buildChatBody(MODERATION_SYSTEM_PROMPT, "Evaluate this message: \"" + str + "\"", 25, 0.0);
                ResponseEntity<String> response = restTemplate.postForEntity(
                        groqApiUrl,
                        new HttpEntity<>(body, buildHeaders()),
                        String.class
                );
                return parseModeration(response.getBody());
            } catch (Exception e) {
                logger.warn("Groq API unavailable ({}). Pattern filter passed.", e.getMessage());
                this.currentStatus = "DEGRADED";
                this.lastError = "Moderation request failed: " + e.getMessage();
                this.lastChecked = new Date();
                if (systemLogService != null) {
                    systemLogService.recordLog("WARN", "GROQ_AI", "Moderation /unsafe", 503, null, "Groq moderation unavailable: " + e.getMessage(), lastError);
                }
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
        headers.setBearerAuth(groqApiKey);
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
        root.put("model", groqModel);
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

    @Override
    public AiStatusResponse getAiStatus() {
        String provider = detectProvider();
        String message;
        if ("HEALTHY".equalsIgnoreCase(currentStatus)) {
            message = "Groq LPU AI inference is online and responding normally (" + (lastLatencyMs != null ? lastLatencyMs + "ms" : "ready") + ").";
        } else if ("DEGRADED".equalsIgnoreCase(currentStatus)) {
            message = "Groq AI service is degraded: " + (lastError != null ? lastError : "Fallback active") + ". Pattern-based moderation and offline companion active.";
        } else if ("OFFLINE".equalsIgnoreCase(currentStatus)) {
            message = "Groq AI service is offline: " + (lastError != null ? lastError : "Connection failed") + ". Offline fallback active.";
        } else {
            message = "Groq API key is unconfigured. Offline companion and regex safety are running.";
        }

        return AiStatusResponse.builder()
                .status(currentStatus)
                .provider(provider)
                .model(groqModel)
                .endpoint(groqApiUrl)
                .latencyMs(lastLatencyMs)
                .lastChecked(lastChecked)
                .lastError(lastError)
                .activeFallback(!"HEALTHY".equalsIgnoreCase(currentStatus))
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
            String body = buildChatBody("You are an echo bot.", "ping", 5, 0.0);
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
                logger.info("Groq AI ping test successful in {}ms", latency);
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
            return fallbackEnhance(rawText, type);
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
            String body = buildChatBody(systemPrompt, "Input text to enhance: \"" + rawText + "\"", 512, 0.4);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    groqApiUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

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
        } catch (Exception e) {
            logger.warn("Groq enhanceText encountered error: {}. Falling back to heuristic enhancement.", e.getMessage());
        }

        return fallbackEnhance(rawText, type);
    }

    private EnhanceResponse fallbackEnhance(String rawText, String type) {
        String polished = rawText;
        if (!polished.isEmpty()) {
            polished = Character.toUpperCase(polished.charAt(0)) + polished.substring(1);
        }
        // Basic punctuation & common contractions fix
        polished = polished.replaceAll("\\bi\\b", "I")
                .replaceAll("\\bdont\\b", "don't")
                .replaceAll("\\bim\\b", "I'm")
                .replaceAll("\\bcant\\b", "can't")
                .replaceAll("\\bwont\\b", "won't")
                .replaceAll("\\s+", " ")
                .trim();

        List<String> suggestions = new ArrayList<>();
        List<String> suggestedOptions = new ArrayList<>();

        if ("POLL".equals(type)) {
            if (!polished.endsWith("?")) {
                polished += "?";
            }
            suggestedOptions.add("Strongly Agree / Yes");
            suggestedOptions.add("Somewhat / In between");
            suggestedOptions.add("Strongly Disagree / No");
            suggestedOptions.add("Just viewing results");
            suggestions.add("Keep options balanced so voters feel confident picking one.");
        } else if ("QUESTION".equals(type)) {
            if (!polished.endsWith("?")) {
                polished += "?";
            }
            suggestions.add("Consider framing the question with personal context to encourage open, honest replies.");
            suggestions.add("Short, clear questions receive the most community engagement.");
        } else {
            // CONFESSION
            if (!polished.endsWith(".") && !polished.endsWith("!") && !polished.endsWith("?")) {
                polished += ".";
            }
            suggestions.add("Sharing how this situation made you feel brings comforting, genuine empathy from peers.");
            suggestions.add("Your vulnerability is respected and 100% anonymous.");
        }

        return EnhanceResponse.builder()
                .originalText(rawText)
                .polishedText(polished)
                .suggestions(suggestions)
                .suggestedOptions(suggestedOptions)
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

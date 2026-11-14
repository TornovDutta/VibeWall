package org.example.vibewall.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.example.vibewall.DTO.AiStatusResponse;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@Primary
public class HuggingFaceAiServiceImpl implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceAiServiceImpl.class);

    @Value("${hf.token:${HF_TOKEN:${ai.token:${AI_TOKEN:}}}}")
    private String hfToken;

    @Value("${hf.model:${HF_MODEL:${ai.model:${AI_MODEL:Qwen/Qwen2.5-72B-Instruct}}}}")
    private String hfModel;

    @Value("${hf.api.url:${HF_API_URL:${ai.api.url:${AI_API_URL:https://router.huggingface.co/v1/chat/completions}}}}")
    private String hfUrl;

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
        if (hfToken != null) {
            String trimmedToken = hfToken.trim();
            // Automatically switch endpoint & model if Groq key is supplied
            if (trimmedToken.startsWith("gsk_") && hfUrl.contains("huggingface.co")) {
                this.hfUrl = "https://api.groq.com/openai/v1/chat/completions";
                if (this.hfModel == null || this.hfModel.contains("Qwen") || this.hfModel.isBlank()) {
                    this.hfModel = "llama-3.3-70b-versatile";
                }
                logger.info("Auto-configured Groq API endpoint for Groq key prefix 'gsk_' (model: {})", this.hfModel);
            }
            // Automatically switch endpoint & model if OpenRouter key is supplied
            else if (trimmedToken.startsWith("sk-or-v1-") && hfUrl.contains("huggingface.co")) {
                this.hfUrl = "https://openrouter.ai/api/v1/chat/completions";
                if (this.hfModel == null || this.hfModel.contains("Qwen/Qwen2.5-72B-Instruct")) {
                    this.hfModel = "meta-llama/llama-3.2-3b-instruct:free";
                }
                logger.info("Auto-configured OpenRouter API endpoint for OpenRouter key prefix 'sk-or-v1-' (model: {})", this.hfModel);
            }
        }
        logger.info("AI Service initialized with endpoint: {} and model: {}", hfUrl, hfModel);
    }

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
            return generateOfflineCompanionResponse(prompt);
        }

        try {
            String body = buildChatBody(CHATBOT_SYSTEM_PROMPT, prompt, 512, 0.7);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    hfUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );
            String reply = extractContent(response.getBody());
            if (reply != null && !reply.isBlank()) {
                return reply;
            }
            return generateOfflineCompanionResponse(prompt);
        } catch (HttpStatusCodeException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.warn("AI Provider API error [{}]: {}", e.getStatusCode(), errorBody);
            this.currentStatus = "DEGRADED";
            this.lastError = "HTTP " + e.getStatusCode() + ": " + (errorBody.length() > 200 ? errorBody.substring(0, 200) : errorBody);
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "AI_SERVICE", "Chatbot /get-response", e.getStatusCode().value(), null, "AI Provider returned error: " + e.getStatusCode(), lastError);
            }

            if (errorBody.contains("pre-paid credits") || errorBody.contains("credits") || e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                logger.warn("AI Provider quota reached / requires pre-paid credits. To use 100% free AI without credit card, switch to Groq (https://console.groq.com/keys) or OpenRouter in Backend/.env");
                return generateOfflineCompanionResponse(prompt) +
                        "\n\n*(Note: Cloud AI quota reached. To use 100% free live AI, switch to Groq in Backend/.env - see README)*";
            } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("AI Provider unauthorized. Please check token permissions in Backend/.env.");
                return generateOfflineCompanionResponse(prompt);
            }

            return generateOfflineCompanionResponse(prompt);
        } catch (Exception e) {
            logger.error("AI request failed: {}", e.getMessage(), e);
            this.currentStatus = "DEGRADED";
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            this.lastChecked = new Date();
            if (systemLogService != null) {
                systemLogService.recordLog("WARN", "AI_SERVICE", "Chatbot /get-response", 500, null, "AI request failed: " + e.getMessage(), lastError);
            }
            return generateOfflineCompanionResponse(prompt);
        }
    }

    /**
     * Resilient offline empathetic companion fallback that responds warmly even if
     * external cloud AI is offline, rate-limited, or out of credits.
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
                this.currentStatus = "DEGRADED";
                this.lastError = "Moderation request failed: " + e.getMessage();
                this.lastChecked = new Date();
                if (systemLogService != null) {
                    systemLogService.recordLog("WARN", "AI_SERVICE", "Moderation /unsafe", 503, null, "AI moderation unavailable: " + e.getMessage(), lastError);
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

    @Override
    public AiStatusResponse getAiStatus() {
        String provider = detectProvider();
        String message;
        if ("HEALTHY".equalsIgnoreCase(currentStatus)) {
            message = "AI provider (" + provider + ") is online and responding normally (" + (lastLatencyMs != null ? lastLatencyMs + "ms" : "ready") + ").";
        } else if ("DEGRADED".equalsIgnoreCase(currentStatus)) {
            message = "AI provider is degraded: " + (lastError != null ? lastError : "Fallback active") + ". Pattern-based moderation and offline companion active.";
        } else if ("OFFLINE".equalsIgnoreCase(currentStatus)) {
            message = "AI provider is offline: " + (lastError != null ? lastError : "Connection failed") + ". Offline fallback active.";
        } else {
            message = "AI token is unconfigured. Offline companion and regex safety are running.";
        }

        return AiStatusResponse.builder()
                .status(currentStatus)
                .provider(provider)
                .model(hfModel)
                .endpoint(hfUrl)
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
        if (hfToken == null || hfToken.isBlank()) {
            this.currentStatus = "UNCONFIGURED";
            this.lastError = "AI token is not configured in Backend/.env";
            return getAiStatus();
        }

        long start = System.currentTimeMillis();
        try {
            String body = buildChatBody("You are an echo bot.", "ping", 5, 0.0);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    hfUrl,
                    new HttpEntity<>(body, buildHeaders()),
                    String.class
            );

            long latency = System.currentTimeMillis() - start;
            if (response.getStatusCode().is2xxSuccessful()) {
                this.currentStatus = "HEALTHY";
                this.lastLatencyMs = latency;
                this.lastError = null;
                logger.info("AI ping test successful in {}ms", latency);
            } else {
                this.currentStatus = "DEGRADED";
                this.lastLatencyMs = latency;
                this.lastError = "Returned non-2xx status: " + response.getStatusCode();
                if (systemLogService != null) {
                    systemLogService.recordLog("WARN", "AI_SERVICE", "POST /admin/ai-test", response.getStatusCode().value(), "admin", "AI ping test non-2xx response", lastError);
                }
            }
        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "OFFLINE";
            this.lastLatencyMs = latency;
            this.lastError = "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "AI_SERVICE", "POST /admin/ai-test", e.getStatusCode().value(), "admin", "AI ping test failed: " + e.getStatusCode(), lastError);
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            this.currentStatus = "OFFLINE";
            this.lastLatencyMs = latency;
            this.lastError = e.getMessage() != null ? e.getMessage() : e.toString();
            if (systemLogService != null) {
                systemLogService.recordLog("CRITICAL", "AI_SERVICE", "POST /admin/ai-test", 503, "admin", "AI ping test failed with exception", lastError);
            }
        }
        return getAiStatus();
    }

    private String detectProvider() {
        if (hfUrl == null) return "Unknown";
        String lower = hfUrl.toLowerCase(Locale.ROOT);
        if (lower.contains("groq.com")) return "Groq";
        if (lower.contains("openrouter.ai")) return "OpenRouter";
        if (lower.contains("huggingface.co")) return "Hugging Face";
        return "Custom LLM Provider";
    }
}

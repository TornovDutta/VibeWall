package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.DailyVibeResponse;
import org.example.vibewall.DTO.VibeCheckRequest;
import org.example.vibewall.DTO.VibeCheckResponse;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.VibeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VibeServiceImpl implements VibeService {

    private static final Logger logger = LoggerFactory.getLogger(VibeServiceImpl.class);
    private final AiService aiService;
    private final Random random = new Random();

    private static final List<DailyVibeResponse> VIBE_QUOTES = Arrays.asList(
            DailyVibeResponse.builder()
                    .quote("Be gentle with yourself. You are doing the best you can with what you have today.")
                    .author("VibeWall Sanctuary")
                    .category("Self-Love")
                    .emoji("🌱")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("What feels like a dead end is often just an invitation to take a new turn.")
                    .author("Campus Wisdom")
                    .category("Perseverance")
                    .emoji("✨")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("Venting is not giving up; it is clearing the clouds so the sun can shine again.")
                    .author("Anonymous Friend")
                    .category("Release")
                    .emoji("🌧️")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("You don't have to carry every secret alone. Letting it out lightens the soul.")
                    .author("Night Thoughts")
                    .category("Comfort")
                    .emoji("💜")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("No feeling is final. The weight on your chest tonight will feel lighter tomorrow.")
                    .author("Rainer Maria Rilke")
                    .category("Hope")
                    .emoji("🌅")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("Sometimes simply making it through a difficult day is the greatest victory.")
                    .author("Everyday Hero")
                    .category("Strength")
                    .emoji("🛡️")
                    .build(),
            DailyVibeResponse.builder()
                    .quote("Your story isn't over just because one chapter felt too heavy to read.")
                    .author("Vibe Collective")
                    .category("Renewal")
                    .emoji("📖")
                    .build()
    );

    @Override
    public DailyVibeResponse getDailyVibe() {
        int dayOfYear = LocalDate.now().getDayOfYear();
        int index = dayOfYear % VIBE_QUOTES.size();
        return VIBE_QUOTES.get(index);
    }

    @Override
    public DailyVibeResponse getRandomVibe() {
        int index = random.nextInt(VIBE_QUOTES.size());
        return VIBE_QUOTES.get(index);
    }

    @Override
    public VibeCheckResponse checkVibe(VibeCheckRequest request) {
        String text = request.getContent() != null ? request.getContent().trim() : "";
        if (text.isEmpty()) {
            return VibeCheckResponse.builder()
                    .vibe("Quiet & Neutral")
                    .emoji("🍃")
                    .sentiment("Neutral")
                    .comfortingTip("Whenever you're ready, say what's on your mind.")
                    .tags(List.of("neutral", "calm"))
                    .build();
        }

        String lower = text.toLowerCase(Locale.ROOT);

        // Smart emotional vibe recognition
        if (lower.contains("stress") || lower.contains("anxious") || lower.contains("anxiety") ||
                lower.contains("overwhelm") || lower.contains("exhaust") || lower.contains("tired") ||
                lower.contains("exam") || lower.contains("burnout")) {
            return VibeCheckResponse.builder()
                    .vibe("Overwhelmed & Seeking Peace")
                    .emoji("🌧️")
                    .sentiment("Stressful")
                    .comfortingTip("Take a slow, deep breath. You are more capable than this moment makes you feel.")
                    .tags(Arrays.asList("vent", "stressed", "seeking-peace"))
                    .build();
        }

        if (lower.contains("love") || lower.contains("crush") || lower.contains("miss him") ||
                lower.contains("miss her") || lower.contains("heart") || lower.contains("fall for")) {
            return VibeCheckResponse.builder()
                    .vibe("Romantic & Longing")
                    .emoji("💌")
                    .sentiment("Passionate")
                    .comfortingTip("Vulnerability is bravery. Honoring your true feelings is always beautiful.")
                    .tags(Arrays.asList("romance", "crush", "heartfelt"))
                    .build();
        }

        if (lower.contains("lonely") || lower.contains("alone") || lower.contains("sad") ||
                lower.contains("cry") || lower.contains("depress") || lower.contains("hurts") ||
                lower.contains("broken") || lower.contains("nobody")) {
            return VibeCheckResponse.builder()
                    .vibe("Heavy Heart & Solitude")
                    .emoji("💙")
                    .sentiment("Melancholy")
                    .comfortingTip("You are never truly alone. This community hears you and holds space for you.")
                    .tags(Arrays.asList("support-needed", "honest", "healing"))
                    .build();
        }

        if (lower.contains("happy") || lower.contains("excited") || lower.contains("grateful") ||
                lower.contains("proud") || lower.contains("smile") || lower.contains("won") ||
                lower.contains("passed") || lower.contains("success")) {
            return VibeCheckResponse.builder()
                    .vibe("Radiant & Victorious")
                    .emoji("✨")
                    .sentiment("Positive")
                    .comfortingTip("Celebrate this victory! Your joy spreads ripples of happiness to everyone reading.")
                    .tags(Arrays.asList("celebration", "joy", "wholesome"))
                    .build();
        }

        if (lower.contains("angry") || lower.contains("mad") || lower.contains("hate") ||
                lower.contains("annoy") || lower.contains("furious") || lower.contains("unfair")) {
            return VibeCheckResponse.builder()
                    .vibe("Fiery Vent")
                    .emoji("🔥")
                    .sentiment("Intense")
                    .comfortingTip("Letting the fire out safely here is healthy. Protect your peace and take back control.")
                    .tags(Arrays.asList("vent", "uncensored", "cathartic"))
                    .build();
        }

        // Deep/philosophical default
        return VibeCheckResponse.builder()
                .vibe("Thoughtful & Reflective")
                .emoji("🌌")
                .sentiment("Introspective")
                .comfortingTip("Every confession carries a piece of truth. Thank you for expressing yours.")
                .tags(Arrays.asList("reflective", "authentic", "story"))
                .build();
    }
}

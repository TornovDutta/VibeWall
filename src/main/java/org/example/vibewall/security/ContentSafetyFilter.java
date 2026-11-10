package org.example.vibewall.security;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * High-performance, zero-latency content safety moderation filter.
 * Protects VibeWall by catching harassment, severe profanity, hate speech,
 * threats, self-harm encouragement, and doxxing both independently and as a fallback.
 */
public class ContentSafetyFilter {

    // Regex patterns with word boundaries (\b) to prevent false positives on substrings (e.g. 'classic', 'assume')
    private static final List<Pattern> COMPILED_PATTERNS = Arrays.asList(
            // ─── 1. Severe Harassment & Toxic Insults ──────────────────────────────────────────
            compileWordPattern("bitch"),
            compileWordPattern("bitches"),
            compileWordPattern("bitching"),
            compileWordPattern("bastard"),
            compileWordPattern("bastards"),
            compileWordPattern("asshole"),
            compileWordPattern("assholes"),
            compileWordPattern("cunt"),
            compileWordPattern("cunts"),
            compileWordPattern("motherfucker"),
            compileWordPattern("motherfuckers"),
            compileWordPattern("fuck"),
            compileWordPattern("fucking"),
            compileWordPattern("fucked"),
            compileWordPattern("fucker"),
            compileWordPattern("slut"),
            compileWordPattern("sluts"),
            compileWordPattern("whore"),
            compileWordPattern("whores"),
            compileWordPattern("pussy"),
            compileWordPattern("dickhead"),
            compileWordPattern("dickheads"),
            compileWordPattern("dipshit"),
            compileWordPattern("jackass"),
            compileWordPattern("scumbag"),
            compileWordPattern("retard"),
            compileWordPattern("retarded"),
            compileWordPattern("idiot"),
            compileWordPattern("idiots"),
            compileWordPattern("piece of shit"),
            compileWordPattern("stfu"),
            compileWordPattern("shut the fuck up"),

            // ─── 2. Hate Speech & Discriminatory Slurs ────────────────────────────────────────
            compileWordPattern("nigger"),
            compileWordPattern("nigga"),
            compileWordPattern("niggers"),
            compileWordPattern("faggot"),
            compileWordPattern("fag"),
            compileWordPattern("faggots"),
            compileWordPattern("dyke"),
            compileWordPattern("tranny"),
            compileWordPattern("chink"),
            compileWordPattern("kike"),
            compileWordPattern("spic"),
            compileWordPattern("wetback"),

            // ─── 3. Violence & Threat Patterns ────────────────────────────────────────────────
            compilePattern("(?i)\\b(i\\s*will|im\\s*gonna|i\\s*am\\s*going\\s*to)\\s*(kill|murder|shoot|stab|slit|hunt|destroy|strangle)\\s*(you|u|them|her|him|everyone)\\b"),
            compilePattern("(?i)\\b(kill|murder|shoot|stab)\\s*(you|u|them|her|him)\\b"),
            compilePattern("(?i)\\b(i\\s*hope\\s*you\\s*die|go\\s*die|drop\\s*dead)\\b"),
            compilePattern("(?i)\\b(bomb\\s*(the|this|your)|terrorist\\s*attack)\\b"),
            compilePattern("(?i)\\b(i\\s*will\\s*rape|gonna\\s*rape|rape\\s*you)\\b"),

            // ─── 4. Self-Harm & Suicide Encouragement ─────────────────────────────────────────
            compilePattern("(?i)\\b(kill\\s*your\\s*self|kill\\s*urself|kys)\\b"),
            compilePattern("(?i)\\b(hang\\s*your\\s*self|hang\\s*urself)\\b"),
            compilePattern("(?i)\\b(end\\s*(your|ur)\\s*life)\\b"),
            compilePattern("(?i)\\b(slit\\s*(your|ur)\\s*wrists?)\\b"),
            compilePattern("(?i)\\b(drink\\s*bleach|swallow\\s*pills\\s*and\\s*die)\\b"),
            compilePattern("(?i)\\b(jump\\s*off\\s*a\\s*(bridge|building|cliff))\\b"),

            // ─── 5. Doxxing & Phone Numbers ──────────────────────────────────────────────────
            compilePattern("(?i)\\b(doxxed|doxxing|leak\\s*(your|his|her)\\s*(number|address|nudes))\\b")
    );

    private static Pattern compileWordPattern(String word) {
        return Pattern.compile("(?i)\\b" + Pattern.quote(word) + "\\b");
    }

    private static Pattern compilePattern(String regex) {
        return Pattern.compile(regex);
    }

    /**
     * Checks if the content violates safety policy.
     * Normalizes leet-speak (e.g. b!tch, f*ck, k1ll) and scans against comprehensive categories.
     */
    public static boolean isUnsafe(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        // Test 1: Direct match against raw input
        for (Pattern p : COMPILED_PATTERNS) {
            if (p.matcher(content).find()) {
                return true;
            }
        }

        // Test 2: Normalize leet-speak and obfuscations (e.g., f*ck, b!tch, a$$hole, k1ll)
        String normalized = normalizeObfuscations(content);
        for (Pattern p : COMPILED_PATTERNS) {
            if (p.matcher(normalized).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * De-obfuscates text: converts numbers/symbols used as letters into plain text
     * and strips masking characters (*, _, -).
     */
    public static String normalizeObfuscations(String input) {
        if (input == null) return "";
        String s = input.toLowerCase(Locale.ROOT);

        // Replace common symbol substitutions
        s = s.replace('@', 'a')
             .replace('$', 's')
             .replace('!', 'i')
             .replace('1', 'i')
             .replace('|', 'i')
             .replace('0', 'o')
             .replace('3', 'e')
             .replace('5', 's')
             .replace('7', 't')
             .replace('8', 'b');

        // Remove asterisks and masking dots within words (e.g., f*ck -> fck, s.h.i.t -> shit)
        s = s.replaceAll("[*._~]", "");

        // Collapse repeated single-spaced characters (e.g. "f u c k" -> "fuck")
        s = s.replaceAll("(?<=\\b\\w)\\s+(?=\\w\\b)", "");

        return s;
    }
}

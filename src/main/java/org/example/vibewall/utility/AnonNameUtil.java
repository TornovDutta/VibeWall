package org.example.vibewall.utility;

public class AnonNameUtil {

    private static final String[] ADJECTIVES = {
        "Silent", "Cosmic", "Mystic", "Hidden", "Shadow", "Velvet", "Neon", "Lunar",
        "Blazing", "Frozen", "Golden", "Silver", "Crimson", "Azure", "Ember", "Storm",
        "Prism", "Radiant", "Phantom", "Hollow", "Vivid", "Serene", "Restless", "Faded"
    };

    private static final String[] NOUNS = {
        "Fox", "Wolf", "Eagle", "Tiger", "Raven", "Phoenix", "Falcon", "Lynx",
        "Viper", "Panther", "Hawk", "Bear", "Owl", "Cobra", "Drake", "Puma",
        "Jaguar", "Crane", "Kestrel", "Moth", "Hyena", "Drifter", "Nomad", "Cipher"
    };

    public static String generate(String seed) {
        if (seed == null || seed.isBlank()) {
            return "Anonymous";
        }
        int h = 0;
        for (int i = 0; i < seed.length(); i++) {
            h = 31 * h + seed.charAt(i);
        }
        h = Math.abs(h);
        String adj = ADJECTIVES[h % ADJECTIVES.length];
        String noun = NOUNS[(h / ADJECTIVES.length) % NOUNS.length];
        return adj + " " + noun;
    }
}

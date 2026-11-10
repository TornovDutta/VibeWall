package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.service.ReactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private static final Logger logger = LoggerFactory.getLogger(ReactionServiceImpl.class);
    private final StringRedisTemplate redisTemplate;

    // Allowed reaction types
    private static final Set<String> ALLOWED_REACTIONS = Set.of("hug", "heart", "relate", "strength");

    // In-memory fallback if Redis is temporarily unreachable
    private final Map<String, Map<String, Integer>> memoryFallback = new ConcurrentHashMap<>();

    private static final Duration TTL_12_HOURS = Duration.ofHours(12);

    @Override
    public Map<String, Integer> getReactions(String confessionId) {
        if (confessionId == null || confessionId.isBlank()) {
            return getDefaultReactions();
        }

        String key = "reactions:" + confessionId;
        try {
            Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
            Map<String, Integer> result = getDefaultReactions();
            for (Map.Entry<Object, Object> entry : raw.entrySet()) {
                String k = entry.getKey().toString();
                if (ALLOWED_REACTIONS.contains(k)) {
                    result.put(k, Integer.parseInt(entry.getValue().toString()));
                }
            }
            return result;
        } catch (Exception e) {
            logger.warn("Redis unavailable for reactions read, using in-memory: {}", e.getMessage());
            return memoryFallback.getOrDefault(confessionId, getDefaultReactions());
        }
    }

    @Override
    public Map<String, Integer> addReaction(String confessionId, String reactionType) {
        if (confessionId == null || reactionType == null) {
            return getDefaultReactions();
        }

        String normalizedType = reactionType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_REACTIONS.contains(normalizedType)) {
            return getReactions(confessionId);
        }

        String key = "reactions:" + confessionId;
        try {
            redisTemplate.opsForHash().increment(key, normalizedType, 1);
            redisTemplate.expire(key, TTL_12_HOURS);
            return getReactions(confessionId);
        } catch (Exception e) {
            logger.warn("Redis unavailable for reactions write, using in-memory: {}", e.getMessage());
            Map<String, Integer> map = memoryFallback.computeIfAbsent(confessionId, k -> getDefaultReactions());
            map.put(normalizedType, map.getOrDefault(normalizedType, 0) + 1);
            return new HashMap<>(map);
        }
    }

    private Map<String, Integer> getDefaultReactions() {
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("hug", 0);
        defaults.put("heart", 0);
        defaults.put("relate", 0);
        defaults.put("strength", 0);
        return defaults;
    }
}

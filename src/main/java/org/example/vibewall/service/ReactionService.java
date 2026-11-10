package org.example.vibewall.service;

import java.util.Map;

public interface ReactionService {
    Map<String, Integer> getReactions(String confessionId);
    Map<String, Integer> addReaction(String confessionId, String reactionType);
}

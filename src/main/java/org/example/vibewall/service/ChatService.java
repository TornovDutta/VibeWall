package org.example.vibewall.service;

import org.example.vibewall.DTO.ChatRequest;
import org.example.vibewall.DTO.ChatResponse;

public interface ChatService {
    ChatResponse chat(ChatRequest request);
}

package org.example.vibewall.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ChatRequest;
import org.example.vibewall.DTO.ChatResponse;
import org.example.vibewall.service.AiService;
import org.example.vibewall.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AiService aiService;

    @Value("${hf.model:${HF_MODEL:Qwen/Qwen2.5-72B-Instruct}}")
    private String model;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String reply = aiService.getResponse(request.getMessage());
        return ChatResponse.builder()
                .reply(reply)
                .model(model)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

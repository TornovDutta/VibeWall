package org.example.vibewall.service.serviceImple;

import org.example.vibewall.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

//@Service
//@Primary
public class OpenAiServiceImplement implements AiService {
    private final ChatClient chatClient;

    public OpenAiServiceImplement(OpenAiChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    @Override
    public String getResponse(String prompt){
        return chatClient.prompt().user(prompt).call().content().trim();
    }
    @Override
    public boolean unSafe(String str) {
        String prompt = """
                You are a content safety checker for the platform VibeWall.

                VibeWall is an anonymous and secure emotion-sharing platform for students. 
                It allows emotional expression but strictly prohibits harmful, offensive, or unsafe content.

                ### Platform Principles ###
                1. Content must not include hate speech, harassment, bullying, or threats.
                2. Content must not promote self-harm, violence, discrimination, or illegal activity.
                3. Content must not contain explicit, abusive, or sexual content.
                4. Emotional sharing is encouraged, but safety and positivity are priorities.

                Analyze the following user message and respond **only with a single digit**:
                - Respond **1** if the message violates any principle.
                - Respond **0** if the message fully follows all principles.

                Message: "%s"
                """.formatted(str);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();


        if (response.equals("1")) return true;
        if (response.equals("0")) return false;


        return true;
    }
}

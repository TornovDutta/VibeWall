package org.example.vibewall.DTO;

import java.time.LocalDateTime;

public record FeedbackResponse(String id, String content, LocalDateTime dateTime) {
}

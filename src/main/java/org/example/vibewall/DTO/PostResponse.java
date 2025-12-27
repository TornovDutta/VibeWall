package org.example.vibewall.DTO;

import org.example.vibewall.model.Feedback;

import java.util.List;

public record PostResponse(String id, String content, List<Feedback> feedbacks) {
}

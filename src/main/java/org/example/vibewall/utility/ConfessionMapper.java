package org.example.vibewall.utility;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.ConfessionResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Feedback;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfessionMapper {
    private final Encryption encryption;
    public ConfessionResponse toDTO(Confession confession) {
        String content=encryption.decode(confession.getContent());
        List<Feedback> feedbackList=new ArrayList<>();

        for(Feedback feedback:confession.getFeedbacks()){
            Feedback feedback1=new Feedback();
            feedback1.setId(feedback.getId());
            feedback1.setContent(encryption.decode(feedback.getContent()));
            feedback1.setDate(feedback.getDate());
            feedbackList.add(feedback1);
        }

        return new ConfessionResponse(confession.getId(),content,feedbackList);
    }

    public List<ConfessionResponse> toDTO(List<Confession> confessions) {
        return confessions.stream()
                .map(this::toDTO)
                .toList();
    }
}

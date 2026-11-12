package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private String id;
    private String targetUserId;
    private String targetAlias;
    private String askerAlias;
    private boolean isAnonymous;
    private String content;
    private String status; // PENDING, ANSWERED, DISMISSED
    private String answer;
    private boolean isPublic;
    private Date createdAt;
    private Date answeredAt;
}

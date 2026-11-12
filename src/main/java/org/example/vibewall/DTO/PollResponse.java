package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollResponse {
    private String id;
    private String question;
    private String category;
    private String userId;
    private List<PollOptionResponse> options;
    private int totalVotes;
    private Integer userVotedOptionId; // null if current user has not voted yet
    private boolean active;
    private Date createdAt;
    private Date expiresAt;
}

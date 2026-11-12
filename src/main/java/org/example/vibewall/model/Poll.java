package org.example.vibewall.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "polls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poll {

    @Id
    private String id;

    private String question;

    private String category; // e.g. "General", "Relationships", "Hot Takes", "Life", "Work"

    private String userId; // Creator user ID

    @Builder.Default
    private List<PollOption> options = new ArrayList<>();

    @Builder.Default
    private Set<String> votedUserIds = new HashSet<>();

    @Builder.Default
    private Map<String, Integer> userVotes = new HashMap<>(); // userId -> optionId

    private int totalVotes;

    @CreatedDate
    private Date createdAt;

    @Indexed(expireAfter = "PT168H") // Auto-cleanup after 7 days
    private Date expiresAt;

    public boolean isActive() {
        return expiresAt == null || new Date().before(expiresAt);
    }
}

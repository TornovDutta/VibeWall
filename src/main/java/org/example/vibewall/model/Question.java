package org.example.vibewall.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    private String id;

    @Indexed
    private String targetUserId; // Wall owner receiving the question

    private String askerUserId; // Asker user ID if logged in and not anonymous

    private String askerAlias; // Alias or pseudonym displayed on the question

    private boolean isAnonymous; // true = 100% anonymous, false = with identity

    private String content; // The question text

    @Builder.Default
    private String status = "PENDING"; // PENDING, ANSWERED, DISMISSED

    private String answer; // The wall owner's reply

    private boolean isPublic; // true = visible on owner's public Q&A wall, false = private

    @CreatedDate
    @Indexed(expireAfter = "PT720H") // Retention period: 30 days
    private Date createdAt;

    private Date answeredAt;
}

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

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    @Indexed
    private String userId; // recipient (owner of the confession)

    private String actorId; // who triggered the action (optional)

    private String confessionId;

    private String type; // REACTION, COMMENT, POST_CREATED, SYSTEM

    private String title;

    private String message;

    private boolean read;

    @CreatedDate
    @Indexed(expireAfter = "P7D") // 7 days retention
    private Date createdAt;
}

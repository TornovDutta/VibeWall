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

@Document(collection = "system_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog {

    @Id
    private String id;

    private String level; // ERROR, WARN, CRITICAL, INFO

    private String source; // API, AI_SERVICE, MODERATION, AUTH, CLIENT

    private String endpoint; // e.g. "POST /users/confession"

    private Integer statusCode; // e.g. 500, 422, 503, 400

    private String userId; // User ID or "Anonymous"

    private String userAlias; // Display alias (e.g. "Lunar Fox")

    private String message; // Summary error message

    private String details; // Stack trace snippet or diagnostic info

    @CreatedDate
    @Indexed(expireAfter = "P7D") // Retain error logs for 7 days
    private Date timestamp;
}

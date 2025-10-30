package org.example.vibewall.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDelete {
    @Id
    private String id;
    private  String userId;
    private String username;
    private String requestType;
    private String status;
    private LocalDateTime createdAt;

    public RequestDelete(String userId, String requestType) {
        this.userId = userId;
        this.requestType = requestType;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
}

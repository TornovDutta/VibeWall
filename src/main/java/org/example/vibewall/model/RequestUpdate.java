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
public class RequestUpdate {
    @Id
    private String id;

    private String userId;
    private String oldUsername;
    private String newUsername;
    private String oldPassword;
    private String newPassword;
    private String status;
    private LocalDateTime dateTime;



    public RequestUpdate(Users user) {
        this.userId = user.getId();
        this.oldUsername = user.getUsername();
        this.oldPassword = user.getPassword();
        this.status = "PENDING";
        this.dateTime=LocalDateTime.now();
    }
}

package org.example.vibewall.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vibewall.annotation.StrongPassword;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection= "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @NotBlank
    private String id;
    @NotBlank
    private String username;
    @StrongPassword
    @NotBlank
    private String password;
    private String role="USER";

    private int points = 100;
    private boolean banned = false;
    private String banReason;
    private java.util.Date bannedAt;

    public Users(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.points = 100;
        this.banned = false;
    }

    public Users( String username, String password, String role) {

        this.username = username;
        this.password = password;
        this.role = role;

    }

    public Users(String id,String username) {
        this.username = username;
        this.id= id;

    }
}

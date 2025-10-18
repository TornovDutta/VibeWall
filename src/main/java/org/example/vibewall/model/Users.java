package org.example.vibewall.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "users")
@Data
public class Users {
    @Id
    private String id;
    private String username;
    private String password;
    private String role="USER";

    public Users( String username, String password, String role) {

        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Users(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

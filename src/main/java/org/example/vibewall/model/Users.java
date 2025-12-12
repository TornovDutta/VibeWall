package org.example.vibewall.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection= "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    private String id;
    private String username;
    private String password;
    private String role="USER";
    private List<Confession> confessionList;

    public Users(String username, String password,String role) {
        this.username = username;
        this.password = password;
        this.role=role;
    }
}

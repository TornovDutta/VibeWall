package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest implements Serializable{
    private String name;
    private String password;
    private String role;

    public UserRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }
}

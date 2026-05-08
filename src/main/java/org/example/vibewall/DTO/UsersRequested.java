package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequested implements Serializable{
    private String name;
    private String password;
    private String role;

    public UsersRequested(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
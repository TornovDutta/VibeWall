package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse implements Serializable{
    private String id;
    private String name;
    private String role;
    private int points;
    private boolean banned;
    private String banReason;

    public UserResponse(String id, String name) {
        this.id = id;
        this.name = name;
        this.role = "USER";
        this.points = 100;
        this.banned = false;
    }

    public UserResponse(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.points = 100;
        this.banned = false;
    }
}

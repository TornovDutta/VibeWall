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


}

package org.example.vibewall.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersResponse implements Serializable{
    private String id;
    private String name ;


}
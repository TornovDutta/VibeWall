package org.example.vibewall.service.utilly;

import org.example.vibewall.DTO.UsersDTO;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsersMapper {
    public UsersDTO toDto(Users user){
        return new UsersDTO(
                user.getId(),
                user.getUsername()
        );

    }
    public List<UsersDTO> toDtoList(List<Users> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public Users toEntity(UsersDTO usersDTO){
        return new Users(
               usersDTO.id(),
               usersDTO.username()
        );
    }
}

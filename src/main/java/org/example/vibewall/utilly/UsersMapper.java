package org.example.vibewall.utilly;

import org.example.vibewall.DTO.UsersResponseDTO;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UsersMapper {
    public UsersResponseDTO toDto(Users user){
        return new UsersResponseDTO(
                user.getId(),
                user.getUsername()
        );

    }
    public List<UsersResponseDTO> toDtoList(List<Users> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    public Users toEntity(UsersResponseDTO usersResponseDTO){
        return new Users(
               usersResponseDTO.id(),
               usersResponseDTO.username()
        );
    }
    public List<Users> toEntityList(List<UsersResponseDTO> usersResponseDTOS){
        return usersResponseDTOS.stream().map(this::toEntity).collect(Collectors.toList());
    }
}

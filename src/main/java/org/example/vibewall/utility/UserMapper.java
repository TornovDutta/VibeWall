package org.example.vibewall.utility;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final Encryption encryption;
    public UsersResponse toDTO(Users users) {
        String name = encryption.decode(users.getUsername());
        return new UsersResponse(users.getId(), name);
    }

    public List<UsersResponse> toDTO(List<Users> users) {
        return users.stream()
                .map(this::toDTO)
                .toList();
    }



}
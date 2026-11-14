package org.example.vibewall.utility;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.UserResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final Encryption encryption;

    public UserResponse toDTO(Users users) {
        String name = encryption.decode(users.getUsername());
        return UserResponse.builder()
                .id(users.getId())
                .name(name)
                .role(users.getRole())
                .points(users.getPoints())
                .banned(users.isBanned())
                .banReason(users.getBanReason())
                .build();
    }

    public List<UserResponse> toDTO(List<Users> users) {
        return users.stream()
                .map(this::toDTO)
                .toList();
    }
}

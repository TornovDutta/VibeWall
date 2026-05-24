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
        return new UserResponse(users.getId(), name);
    }

    public List<UserResponse> toDTO(List<Users> users) {
        return users.stream()
                .map(this::toDTO)
                .toList();
    }
}

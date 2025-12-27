package org.example.vibewall.utility;

import lombok.RequiredArgsConstructor;
import org.example.vibewall.DTO.PostResponse;
import org.example.vibewall.DTO.UsersResponse;
import org.example.vibewall.encryption.Encryption;
import org.example.vibewall.model.Confession;
import org.example.vibewall.model.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final Encryption encryption;
    public PostResponse toDTO(Confession confession) {
        String content=encryption.decode(confession.getContent());

        return new PostResponse(confession.getId(),content,confession.getFeedbacks());
    }

    public List<PostResponse> toDTO(List<Confession> confessions) {
        return confessions.stream()
                .map(this::toDTO)
                .toList();
    }
}

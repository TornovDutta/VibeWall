package org.example.vibewall.service;

import jakarta.validation.constraints.NotBlank;
import org.example.vibewall.model.RefreshToken;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(@NotBlank String id);

    RefreshToken findByToken(String refreshToken);

    void deleteByUserId(String userId);
}

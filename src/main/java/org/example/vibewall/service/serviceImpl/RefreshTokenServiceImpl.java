package org.example.vibewall.service.serviceImpl;

import org.example.vibewall.model.RefreshToken;
import org.example.vibewall.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.example.vibewall.service.RefreshTokenService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repo;

    private final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60;

    public RefreshToken createRefreshToken(String userId) {
        repo.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(REFRESH_EXPIRATION))
                .build();

        return repo.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return repo.findByToken(token)
                .map(this::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    public void deleteByUserId(String userId) {
        repo.deleteByUserId(userId);
    }
}

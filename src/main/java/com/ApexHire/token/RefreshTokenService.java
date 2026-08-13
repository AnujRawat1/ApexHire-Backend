package com.ApexHire.token;

import com.ApexHire.common.exception.InvalidRefreshTokenException;
import com.ApexHire.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long IDLE_TIMEOUT_SECONDS = 2 * 60 * 60;

    public String createRefreshToken(User user) {

        byte[] randomBytes = new byte[64];

        new SecureRandom().nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                                .withoutPadding()
                                .encodeToString(randomBytes);

        Instant now = Instant.now();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(rawToken))
                .createdAt(now)
                .expiresAt(now.plusSeconds(IDLE_TIMEOUT_SECONDS))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public RefreshToken validateRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                ));

        Instant now = Instant.now();

        /*
         * Check whether the user has been idle
         * for more than 2 hours.
         */
        if (refreshToken.getCreatedAt()
                .plusSeconds(IDLE_TIMEOUT_SECONDS)
                .isBefore(now)) {

            refreshTokenRepository.delete(refreshToken);

            throw new InvalidRefreshTokenException(
                    "Refresh token expired due to inactivity"
            );
        }

        return refreshToken;
    }

//    public void updateLastUsed(RefreshToken refreshToken) {
//        Instant now = Instant.now();
//        refreshToken.setLastUsedAt(now);
//        refreshToken.setExpiresAt(now.plusSeconds(IDLE_TIMEOUT_SECONDS));
//        refreshTokenRepository.save(refreshToken);
//    }

    public String rotateRefreshToken(RefreshToken oldToken, User user) {
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(user);
    }

    public void revokeRefreshToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                                                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        refreshTokenRepository.delete(refreshToken);
    }
    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }


}
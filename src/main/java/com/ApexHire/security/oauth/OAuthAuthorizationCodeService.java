package com.ApexHire.security.oauth;

import com.ApexHire.common.exception.InvalidRefreshTokenException;
import com.ApexHire.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OAuthAuthorizationCodeService {

    private final OAuthAuthorizationCodeRepository repository;

    private static final long CODE_EXPIRATION_SECONDS = 60;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createCode(User user) {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String code = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        OAuthAuthorizationCode authorizationCode = OAuthAuthorizationCode.builder()
                        .code(code)
                        .userId(user.getId())
                        .expiresAt(
                                Instant.now()
                                        .plusSeconds(
                                                CODE_EXPIRATION_SECONDS
                                        )
                        )
                        .used(false)
                        .build();

        repository.save(authorizationCode);

        return code;
    }

    public String consumeCode(String code) {

        OAuthAuthorizationCode authorizationCode = repository.findByCode(code)
                        .orElseThrow(() -> new InvalidRefreshTokenException("Invalid OAuth authorization code"));

        if (authorizationCode.isUsed()) {
            throw new InvalidRefreshTokenException("OAuth authorization code has already been used");
        }

        if (authorizationCode.getExpiresAt().isBefore(Instant.now())) {

            repository.delete(authorizationCode);
            throw new InvalidRefreshTokenException("OAuth authorization code has expired");
        }

        authorizationCode.setUsed(true);

        repository.save(authorizationCode);

        return authorizationCode.getUserId();
    }
}
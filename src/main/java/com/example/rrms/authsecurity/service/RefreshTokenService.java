package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.config.SecurityProperties;
import com.example.rrms.authsecurity.domain.modal.RefreshToken;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.repository.RefreshTokenRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTenantId(user.getTenantId());
        refreshToken.setTokenHash(sha256(raw));
        refreshToken.setExpiresAt(Instant.now().plus(securityProperties.jwt().refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);
        return raw;                                   // only time the raw value exists
    }

    /** Validates, revokes the used token (rotation), returns the owner. */
    @Transactional(noRollbackFor = AuthenticationException.class)
    public User consume(String raw) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(sha256(raw))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {                         // reuse of a rotated token => likely theft
            refreshTokenRepository.revokeAllForUser(refreshToken.getUserId());
            throw new BadCredentialsException("Refresh token reuse detected");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        refreshToken.setRevoked(true);

        User user = userRepository.findById(refreshToken.getUserId()).orElseThrow(() -> new BadCredentialsException("Invalid"));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BadCredentialsException("Invalid");
        return user;
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private static String sha256(String string) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(string.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
